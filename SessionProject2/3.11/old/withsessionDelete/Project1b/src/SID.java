import groupMembership.Server;


public class SID {//session id

	String sess_num;
	Server server;
	public SID(String sess_num, Server server) {
		super();
		this.sess_num = sess_num;
		this.server = server;
	}
	public String getSess_num() {
		return sess_num;
	}
	public void setSess_num(String sess_num) {
		this.sess_num = sess_num;
	}
	public Server getServer() {
		return server;
	}
	public void setServer(Server server) {
		this.server = server;
	}
	
}
