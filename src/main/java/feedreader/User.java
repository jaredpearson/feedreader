package feedreader;

public class User {
	private Integer id;
	private String email;
	
	public User() {
	}
	
	@Deprecated
	public User(String email) {
		this.email = email;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
}
