package session;
import groupMembership.Server;

import java.sql.Timestamp;
import java.util.ArrayList;


public class SessionTable {

	String sid;

	int version;
	String message;
	Timestamp expiration;
	
	Timestamp discard_time;
	
	ArrayList<Server> locations;
	Server whereFound;
	
	public ArrayList<Server> getLocations() {
		return locations;
	}
	public void setLocations(ArrayList<Server> locations) {
		this.locations = locations;
	}
	public SessionTable(String sid, ArrayList<Server> locations) {
		super();
		this.sid = sid;
		this.locations = locations;
	}
	public SessionTable(int version, String message, Timestamp expiration) {
		super();
		this.version = version;
		this.message = message;
		this.expiration = expiration;
	}
	public SessionTable(String sid, int version) {
		super();
		this.sid = sid;
		this.version = version;
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
	
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public SessionTable(String sid, int version, String message,
			Timestamp expiration) {
		super();
		this.sid = sid;
		this.version = version;
		this.message = message;
		this.expiration = expiration;
	}
	public Server getWhereFound() {
		return whereFound;
	}
	public void setWhereFound(Server whereFound) {
		this.whereFound = whereFound;
	}
	public Timestamp getDiscard_time() {
		return discard_time;
	}
	public void setDiscard_time(Timestamp discard_time) {
		this.discard_time = discard_time;
	}

	
}
