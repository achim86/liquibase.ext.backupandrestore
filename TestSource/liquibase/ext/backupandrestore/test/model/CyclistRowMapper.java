package liquibase.ext.backupandrestore.test.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * RowMapper for {@link liquibase.ext.backupandrestore.test.model.Cyclist}.
 * 
 * @author afinke
 *
 */
public class CyclistRowMapper implements RowMapper<Cyclist> {

	@Override
	public Cyclist mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		Cyclist cyclist = new Cyclist();
		cyclist.setId(resultSet.getInt("id"));
		cyclist.setName(resultSet.getString("name"));
		cyclist.setTeam(resultSet.getString("team"));
		cyclist.setBike(resultSet.getInt("bike"));
		return cyclist;
	}

}
