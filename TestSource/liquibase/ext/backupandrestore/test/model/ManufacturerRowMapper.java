package liquibase.ext.backupandrestore.test.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * RowMapper for {@link liquibase.ext.backupandrestore.test.model.Manufacturer}.
 * 
 * @author afinke
 *
 */
public class ManufacturerRowMapper implements RowMapper<Manufacturer> {

	@Override
	public Manufacturer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setId(resultSet.getInt("id"));
		manufacturer.setName(resultSet.getString("name"));
		return manufacturer;
	}

}
