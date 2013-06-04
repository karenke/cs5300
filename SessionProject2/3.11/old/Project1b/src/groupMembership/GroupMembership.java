package groupMembership;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;


public class GroupMembership extends Thread {

	public static ArrayList<Server> mbrSet = new ArrayList<Server>();
	
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
	
	 private boolean addMbr(Server s) {
		 this.mbrSet.add(s);
		 return true;
	 }
	 
	 public static Server getServer(){
		 if(mbrSet.size() > 0){
			 int index = new Random().nextInt(mbrSet.size());
			 return mbrSet.get(index);
		 }
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Server(ip, 0);
	 }
	 
}
