import java.sql.Timestamp;


public class SessionTable {

	int version;
	String message;
	Timestamp expiration;
	public SessionTable(int version, String message, Timestamp expiration) {
		super();
		this.version = version;
		this.message = message;
		this.expiration = expiration;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Timestamp getExpiration() {
		return expiration;
	}
	public void setExpiration(Timestamp expiration) {
		this.expiration = expiration;
	}
	
}
