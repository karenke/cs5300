package groupMembership;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;


public class GroupMembership extends Thread {

	AmazonSimpleDB awsSDB;
	
	Server current;
	
	
	protected static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	protected static final Lock readlock = rwl.readLock();
	protected static final Lock writelock = rwl.writeLock();
	protected static ArrayList<Server> servers = new ArrayList<Server>();
	  
	public GroupMembership(Server s) throws IOException {
	    this.current = s;
	    this.writelock.lock();
	    this.servers.add(current);
	    this.writelock.unlock();
	    this.awsSDB = new AmazonSimpleDBClient(new PropertiesCredentials(GroupMembership.class.getResourceAsStream("AwsCredentials.properties")));
	    //checkRound();
	}
	
	 private boolean addMembership() {
		 return true;
	 }
	 
	 public static List<Server> getServers() {
		 readlock.lock();
		 @SuppressWarnings("unchecked")
		 ArrayList<Server> ss = (ArrayList<Server>) ((ArrayList<Server>) servers).clone();
		 readlock.unlock();
		 return ss;
	 }


}
