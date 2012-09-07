package liquibase.ext.backupandrestore.test.backup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.Assert;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.ext.backupandrestore.test.model.Cyclist;
import liquibase.ext.backupandrestore.test.model.CyclistRowMapper;
import liquibase.ext.backupandrestore.test.model.Manufacturer;
import liquibase.ext.backupandrestore.test.model.ManufacturerRowMapper;
import liquibase.ext.backupandrestore.test.util.LiquibaseTestUtil;
import liquibase.resource.FileSystemResourceAccessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

/**
 * Regression tests for {@link liquibase.ext.backupandrestore.backup.BackupChange} and
 * {@link liquibase.ext.backupandrestore.restore.RestoreChange}.
 * 
 * @author afinke
 *
 */
@RunWith(Parameterized.class)
@ContextConfiguration("classpath:liquibase/ext/backupandrestore/test/resources/applicationContext.xml")
public class BackupRestoreOracleTest {

	private static FileSystemResourceAccessor resourceAccessor 
		= new FileSystemResourceAccessor("TestSource/liquibase/ext/backupandrestore/test/backup/resources");
	@Autowired
	private DataSource dataSource;
	@Value("${user}")
	private String user;
	@Autowired
	private DataSource dataSource2;
	@Value("${user2}")
	private String user2;
	private String prepareChangeLog;
	private String testChangeLog;
	private boolean isMultiSchema;
	private TestContextManager testContextManager;
	private Database database;
	private Liquibase liquibase;
	private Cyclist[] cyclists = new Cyclist[] {
			new Cyclist(1, "Jan Ullrich", "Telekom", 1),
			new Cyclist(2, "Bradley Wiggins", "Sky", 2),
			new Cyclist(3, "Vincenco Nibali", "Liquigas", 3)
	};
	private Manufacturer[] manufacturers = new Manufacturer[] {
			new Manufacturer(1, "BMC"),
			new Manufacturer(2, "Cannondale"),
			new Manufacturer(3, "Scott")
	};
	private List<Cyclist> cyclistsToProof;
	private JdbcTemplate jdbcTemplate;

	public BackupRestoreOracleTest(String prepareChangeLog, String testChangeLog, boolean isMultiSchema)
			throws Exception {
		this.prepareChangeLog = prepareChangeLog;
		this.testChangeLog = testChangeLog;
		this.isMultiSchema = isMultiSchema;
		testContextManager = new TestContextManager(getClass());
		testContextManager.prepareTestInstance(this);
		LiquibaseTestUtil.cleanSchema(dataSource, user);
		LiquibaseTestUtil.cleanSchema(dataSource2, user2);
		database = DatabaseFactory.getInstance()
				.findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
		if(isMultiSchema) {
			jdbcTemplate = new JdbcTemplate(dataSource2);
		} else {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] tmpData 
			= new Object[][] { { "prepareChangeLog.xml", "testChangeLog.xml", false }, 
							   { "prepareChangeLog.xml", "testChangeLogPrefix.xml", false }, 
						       { "prepareChangeLogMultiSchema.xml", "testChangeLogMultiSchema.xml", true } 
		                     };
		return Arrays.asList(tmpData);
	}

	@Test
	public void testBackup() throws ClassNotFoundException,
			SQLException, LiquibaseException {
		
		/*
		 * Prepare the database with some structure and data
		 */
		liquibase = new Liquibase(prepareChangeLog, resourceAccessor, database);
		liquibase.update(null);
		liquibase.tag("rollback");
		
		cyclistsToProof = queryCyclists();
		for (int i = 0; i < cyclists.length; i++) {
			Assert.assertTrue("Assertion failed for " + cyclists[i].getName(),
					cyclists[i].getId() == cyclistsToProof.get(i).getId() &&
				    cyclists[i].getName().equals(cyclistsToProof.get(i).getName()) &&
				    cyclists[i].getTeam().equals(cyclistsToProof.get(i).getTeam()));
		}

		/*
		 * Make some DML and DDL changes after using the <backup>-Tag
		 */
		liquibase = new Liquibase(testChangeLog, resourceAccessor, database);
		liquibase.update(null);
		cyclists[0] = new Cyclist(1, "Jan Ullrich", "Alpecin", 1);
		cyclists[1] = new Cyclist(2, "Bradley Wiggins", "Alpecin", 2);
		cyclistsToProof = queryCyclists();
		for (int i = 0; i < cyclists.length; i++) {
			Assert.assertTrue("Assertion failed for " + cyclists[i].getName(),
					cyclists[i].getId() == cyclistsToProof.get(i).getId() &&
					cyclists[i].getName().equals(cyclistsToProof.get(i).getName()) &&
					cyclists[i].getTeam().equals(cyclistsToProof.get(i).getTeam()));
		}
		Assert.assertEquals("Column height is missing", 1, checkColumnHeight());
		Assert.assertEquals("Column weight is missing", 1, checkColumnWeight());
		Assert.assertEquals("Table country is missing", 1, checkTableCountry());
		Assert.assertEquals("Table manufacturer is still existent.", 0, checkTableManuFacturer());		
		
		/* 
		 * Rollback the changes
		 */
		liquibase.rollback("rollback", null);
		
		// check data changes were rolled back
		cyclists[0] = new Cyclist(1, "Jan Ullrich", "Telekom", 1);
		cyclists[1] = new Cyclist(2, "Bradley Wiggins", "Sky", 2);
		cyclistsToProof = queryCyclists();
		for (int i = 0; i < cyclists.length; i++) {
			Assert.assertTrue("Assertion failed for " + cyclists[i].getName(),
					cyclists[i].getId() == cyclistsToProof.get(i).getId() &&
			 	    cyclists[i].getName().equals(cyclistsToProof.get(i).getName()) &&
			        cyclists[i].getTeam().equals(cyclistsToProof.get(i).getTeam()));
		}
		// check structure changes were rolled back
		Assert.assertEquals("Column height is still present", 0, checkColumnHeight());
		Assert.assertEquals("Column weight is still present", 0, checkColumnWeight());
		Assert.assertEquals("Table country is still present", 0, checkTableCountry());

		// check drop table was rolled back
		List<Manufacturer> manufacturersToProof = jdbcTemplate.query(
				"SELECT id, name " +
				"FROM manufacturer", new ManufacturerRowMapper());
		for (int i = 0; i < manufacturersToProof.size(); i++) {
			Assert.assertTrue("Assertion failed for " + manufacturers[i].getName(),
					manufacturers[i].getId() == manufacturersToProof.get(i).getId() && 
					manufacturers[i].getName().equals(manufacturersToProof.get(i).getName()));
		}
		
		// if single schema check for PK, FKs and NN Constraints
		if(!isMultiSchema) {
			// check primary key
			String cyclistPK = (String) jdbcTemplate.queryForObject(
					"SELECT constraint_name FROM all_constraints " +
					"WHERE constraint_type = 'P' AND owner = '" + user.toUpperCase() + "' AND table_name = 'CYCLIST'", 
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet resultSet, int i) throws SQLException {
							return resultSet.getString(1);
						}
					});
			Assert.assertEquals("CYCLIST_PK", cyclistPK);
			
			// check foreign keys
			String cyclistFK = jdbcTemplate.queryForObject(
					"SELECT constraint_name FROM all_constraints " +
					"WHERE constraint_type = 'R' AND owner = '" + user.toUpperCase() + "' AND table_name = 'CYCLIST'", 
				    new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet resultSet, int i) throws SQLException {
							return resultSet.getString(1);
						}
					});
			Assert.assertEquals("CYCLIST_TEAM_FK", cyclistFK);
			List<String> bikeFKs = jdbcTemplate.query(
					"SELECT constraint_name FROM all_constraints " +
					"WHERE constraint_type = 'R' AND owner = '" + user.toUpperCase() + "' AND table_name = 'BIKE'", 
				    new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet resultSet, int i) throws SQLException {
							return resultSet.getString(1);
						}
					});
			Assert.assertEquals("BIKE_CYCLIST_FK", bikeFKs.get(0));
			Assert.assertEquals("BIKE_TEAM_FK", bikeFKs.get(1));
			
			// check not null constraints
			List<String> cyclistNN = jdbcTemplate.query(
					"SELECT constraint_name FROM all_constraints " +
					"WHERE constraint_type = 'C' AND owner = '" + user.toUpperCase() + "' AND table_name = 'CYCLIST'" +
					"ORDER BY constraint_name", 
				    new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet resultSet, int i) throws SQLException {
							return resultSet.getString(1);
						}
					});
			Assert.assertEquals("CYC_ID_NN", cyclistNN.get(0));
			Assert.assertEquals("CYC_NAME_NN", cyclistNN.get(1));
		}
		
	}

	private List<Cyclist> queryCyclists() {
		return jdbcTemplate.query(
			"SELECT cyclist.id AS id, cyclist.name AS name, team.name AS team, bike.id AS bike " +
			"FROM cyclist, team, bike " +
			"WHERE cyclist.id = bike.cyclist_id AND cyclist.team_id = team.id " +
			"ORDER BY cyclist.id", new CyclistRowMapper());
	}
	
	private int checkColumnHeight() {
		return jdbcTemplate.queryForInt(
			"SELECT COUNT(*) " +
			"FROM user_tab_columns " +
			"WHERE table_name = 'CYCLIST' AND column_name = 'HEIGHT'");
	}
	
	private int checkColumnWeight() { 
		return jdbcTemplate.queryForInt(
			"SELECT COUNT(*) " +
		 	"FROM user_tab_columns " +
		 	"WHERE table_name = 'CYCLIST' AND column_name = 'WEIGHT'");
	}

	private int checkTableCountry() {
		return jdbcTemplate.queryForInt(
			"SELECT COUNT(*) " +
		 	"FROM user_tables " +
		 	"WHERE table_name = 'COUNTRY'");
	}
	
	private int checkTableManuFacturer() {
		return jdbcTemplate.queryForInt(
			"SELECT COUNT(*) " +
		 	"FROM user_tables " +
		 	"WHERE table_name = 'MANUFACTURER'");
	}

}
