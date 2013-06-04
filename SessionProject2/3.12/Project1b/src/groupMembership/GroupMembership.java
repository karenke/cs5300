package groupMembership;


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;


public class GroupMembership extends Thread {

	public static ArrayList<Server> mbrSet = new ArrayList<Server>();

	public GroupMembership() throws IOException {
	}
	
	
	// Check and Update Member set to maintain the state of the GroupMem
	public static boolean CheckUpdate(Server s, boolean alive) {
		if(alive){
			for(Server e: mbrSet){
				if(isServerEqual(s,e)){
					return false;
				}
			}
			System.out.println("add to mbSet :"+s.port);
			addMbr(s);
			
			for(int i = 0; i < mbrSet.size(); i++){
				System.out.println("mbr "+i+": "+mbrSet.get(i).port);
			}
			return true;
		}
		//not alive
		for(Server e: mbrSet){
			if(isServerEqual(s,e)){
				rmMbr(e);
				System.out.println("remove from mbrSet: "+s.port);
				return true;
			}
		}
		
		
//		if (mbrSet.contains(s) && !alive) {
//			rmMbr(s);
//			System.out.println("remove from mbrSet: "+s.port);
//	
//		}
//		if (!mbrSet.contains(s) && alive) {
//			addMbr(s);
//			System.out.println("add to mbSet :"+s.port);
//			
//		}
		
		
		return false;
	}
		
	 
	// Get a server from server set randomly
	public static Server getServer(){
		if(mbrSet.size() > 0){
			int index = new Random().nextInt(mbrSet.size());
			System.out.println(mbrSet.get(index).port);
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
	

	// Check if the mbrSet is empty-> newly boosted node(server)
	public static boolean isEmpty() {
		return mbrSet.isEmpty();
	}
	// Return a copy of part of the member set
	public static ArrayList<Server> GetMembers(int sz) {
		if (sz >= mbrSet.size())
			return mbrSet;
		
		ArrayList<int[]> visited = new ArrayList<int[]>();
		ArrayList<Server> ss = new ArrayList<Server>();
		int idx;
		for (int i = 0; i < sz; ++ i) {
			do {
				idx = new Random().nextInt(mbrSet.size());
			} while (visited.contains(idx));
			ss.add(mbrSet.get(idx));
		}
		return ss;
	}
	public static boolean SetMembers(ArrayList<Server> ss) {
		mbrSet = ss;
		return true;
	}
	
	
	public static boolean isServerEqual(Server s1, Server s2){
		if(s1.ip.getHostAddress().equals(s2.ip.getHostAddress()) && s1.port == s2.port){
			return true;
		}
		return false;
	}
	
	
	
	private static boolean addMbr(Server s) {
		mbrSet.add(s);
		return true;
	}
	 
	private static boolean rmMbr(Server s) {
		mbrSet.remove(s);
		return true;
	}
}
