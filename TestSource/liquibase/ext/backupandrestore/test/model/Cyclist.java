package liquibase.ext.backupandrestore.test.model;

/**
 * Entity class for cyclists.
 * 
 * @author afinke
 *
 */
public class Cyclist {
	
	private int id;
	private String name;
	private String team;
	private int bike;
	
	public Cyclist() {
		
	}

	public Cyclist(int id, String name, String team, int bike) {
		this.id = id;
		this.name = name;
		this.team = team;
		this.bike = bike;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTeam() {
		return team;
	}
	
	public void setTeam(String team) {
		this.team = team;
	}

	public int getBike() {
		return bike;
	}

	public void setBike(int bike) {
		this.bike = bike;
	}
	
}
