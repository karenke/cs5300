package session;
import groupMembership.Server;

import java.sql.Timestamp;
import java.util.ArrayList;


public class SessionTable {

	int sid;

	int version;
	String message;
	Timestamp expiration;
	
	ArrayList<Server> locations;
	
	public ArrayList<Server> getLocations() {
		return locations;
	}
	public void setLocations(ArrayList<Server> locations) {
		this.locations = locations;
	}
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
	
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}

}
