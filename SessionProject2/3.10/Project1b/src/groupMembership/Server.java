package groupMembership;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class Server {
	
	public InetAddress ip;
	public int port;
	
	public Server() {
	      try {
	         this.ip = InetAddress.getByName("127.0.0.1");
	      } catch (UnknownHostException e) {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	      this.port = 0;
	}
	
	public Server(InetAddress ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
	}
	
	
	public Server(String ipAndPort) {
		String[] parts = ipAndPort.split(":");
	    try {
	    	this.ip = InetAddress.getByName(parts[0]);
	        this.port = new Integer(parts[1]);
	    } catch (UnknownHostException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	   
	 public String getServerString() {
	      return this.ip.getHostAddress()+":"+this.port;
	 }
	   
	 public boolean equals(Server s2) {
		 return (this.ip == s2.ip && this.port == s2.port);
	 }

	
}
