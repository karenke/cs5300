package session;

import groupMembership.GroupMembership;
import groupMembership.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import RPC.RPCClient;

public class SessionHandler {

	//CurrentHashMap is used for session state table and it is global variable that can be shared among sessions
	public static ConcurrentHashMap<String, SessionTable> global = new ConcurrentHashMap<String, SessionTable>();
	protected static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	protected static final Lock readlock = rwl.readLock();
	protected static final Lock writelock = rwl.writeLock();
	  
	public static int sessionTimeOut = 1000;
	public static int delta = 10;
	public static int t = 10;
	

	public static Server localServer;
	public static Server bkServer;
	public static SessionTable getSession(HttpServletRequest request,HttpServletResponse response) throws UnknownHostException{
		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		SessionTable session = null;
		
		int version = 1;
		if(cookies != null){
			for(int i = 0; i < cookies.length; i++){
				if(cookies[i].getName().equals("CS5300PROJ1SESSION")){
					cookie = cookies[i];
	//				System.out.println("cookie: "+cookie.getValue());
				}
			}
		}

		String sessionID;
		if(cookie == null){//new session, there is no cookie
		//	String sessionID = UUID.randomUUID().toString();
			
			sessionID =  request.getSession().getId()+"_"+localServer.ip.getHostAddress()+"_"+localServer.port;
		 
			Timestamp time = new Timestamp(System.currentTimeMillis());
	//		time.setMinutes(time.getMinutes()+5);
		    time.setSeconds(time.getSeconds()+sessionTimeOut+2*delta+t);
			session = new SessionTable(sessionID,version,"Welcome, user!",time);
			writelock.lock();
			global.put(sessionID, session);
			writelock.unlock();
			
			writeBackupServer(session, time);
			
			
		}
		else{//get session from cookie
			
			String[] strs = cookie.getValue().split("[_]");
			String sid = strs[0];
			String server_ip = strs[1];
			String server_port = strs[2];
			version = Integer.parseInt(strs[3]);	
			String pri_ip = strs[4];
			String pri_port = strs[5];
			String backup_ip = strs[6];
			String backup_port = strs[7];
			
			// it should be returned from the corresponding server
		//	session = global.get(sessionID);
		//	System.out.println("session msg: "+ session.getMessage());
			
			ArrayList<Server> servers = new ArrayList<Server>();
			servers.add(new Server(pri_ip,pri_port));
			servers.add(new Server(backup_ip,backup_port));
			try {
				
				sessionID = sid+"_"+server_ip+"_"+server_port;
				readlock.lock();
				session = global.get(sessionID);
				readlock.unlock();
		//		System.out.println(session.sid);
				Timestamp time = new Timestamp(System.currentTimeMillis());
				time.setSeconds(time.getSeconds()+sessionTimeOut+2*delta+t);
				
				if(session == null || session.getVersion() != version){
					if(session == null){
						System.out.println("server doesn't have session");
					}
					else{
						System.out.println(session.getVersion() + " does not match with " + version);
					}
					
					//session is not stored in local server, we should read from others
					System.out.println("session id: "+sessionID);
					session = RPCClient.sessionReadClient(sessionID, version, servers);
					if(session != null){
						System.out.println("hi");
						System.out.println(session.getLocations().get(0).port);
						session.setVersion(version+1);
						writelock.lock();
						global.put(sessionID, session);
						writelock.unlock();
					}
					else{
//						time = new Timestamp(System.currentTimeMillis());
//						time.setSeconds(time.getSeconds()+sessionTimeOut+2*delta+t);
						session = new SessionTable(sessionID,1,"Welcome, user!",time);
						writelock.lock();
						global.put(sessionID, session);
						writelock.unlock();
					}
					
					//write a backup server
					writeBackupServer(session, time);

				}
				else{
					session.setLocations(servers);
					readlock.lock();
					version = global.get(sessionID).getVersion()+1;
					readlock.unlock();
					writelock.lock();
					global.get(sessionID).setVersion(version);
					writelock.unlock();
					
					//write a backup server
					writeBackupServer(session, time);
					
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
//		version = global.get(sessionID).getVersion()+1;
//		global.get(sessionID).setVersion(version);
		System.out.println("get session id: "+ session.getSid());
		return session;

	}
	
	public static HttpSession refresh(HttpServletRequest request,SessionTable session) throws UnknownHostException{
		HttpSession sess = request.getSession();
	//	String sessionID = request.getSession().getId();
		String sessionID = session.getSid();
		Timestamp time = new Timestamp(System.currentTimeMillis());
	//	time.setMinutes(time.getMinutes()+5);
		time.setSeconds(time.getSeconds()+sessionTimeOut+2*delta+t);
		writelock.lock();
		global.get(sessionID).setExpiration(time);	
		writelock.unlock();
	//	String location = request.getLocalAddr()+": "+ request.getServerPort();
		String location = InetAddress.getLocalHost().getHostAddress() + ": " + request.getServerPort();
	//	System.out.println(InetAddress.getLocalHost().getHostAddress());
		readlock.lock();
		Timestamp expr = global.get(sessionID).getExpiration();
		String msg = global.get(sessionID).getMessage();
		readlock.unlock();
	//	System.out.println("refresh msg: "+ msg);
		sess.setAttribute("msg", msg);
		sess.setAttribute("exp", expr);
		sess.setAttribute("location", location);
		return sess;
	}
	
	public static HttpSession replace(HttpServletRequest request,SessionTable session) throws UnknownHostException{
		HttpSession sess = request.getSession();
	//	String sessionID = request.getSession().getId();
		String sessionID = session.getSid();
		String newMsg = request.getParameter("newMsg");
		if(newMsg.length() > 240) {
			newMsg = newMsg.substring(0, 240);
		}
		
		Timestamp time = new Timestamp(System.currentTimeMillis());
	//	time.setMinutes(time.getMinutes()+5);
		time.setSeconds(time.getSeconds()+sessionTimeOut+2*delta+t);
		writelock.lock();
		global.get(sessionID).setExpiration(time);
		global.get(sessionID).setMessage(newMsg);
		writelock.unlock();
		
		Timestamp expr = global.get(sessionID).getExpiration();
		String msg = global.get(sessionID).getMessage();
	//	System.out.println("replace msg: "+ msg);
	//	String location = request.getLocalAddr()+": "+ request.getServerPort();
		String location = InetAddress.getLocalHost().getHostAddress() + ": " + request.getServerPort();
	//	System.out.println(InetAddress.getLocalHost().getHostAddress());
		
		sess.setAttribute("msg", newMsg);
		sess.setAttribute("exp", expr);
		sess.setAttribute("location", location);
		return sess;
	}
	
	public static void removeSession(SessionTable sess,HttpServletRequest request,HttpServletResponse response){
	//	HttpSession session = request.getSession();
	//	session.invalidate();//invalidate the session
		String sessionID = sess.getSid();
		writelock.lock();
		global.remove(sessionID);//remove the session from table
		writelock.unlock();
		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		ArrayList<Server> servers = new ArrayList<Server>();
		int version = 1;
		if(cookies != null){
			for(int i = 0; i < cookies.length; i++){
				if(cookies[i].getName().equals("CS5300PROJ1SESSION")){
					cookie = cookies[i];
					
					String[] strs = cookie.getValue().split("[_]");
//					String sid = strs[0];
//					String server_ip = strs[1];
//					String server_port = strs[2];
					version = Integer.parseInt(strs[3]);	
					String pri_ip = strs[4];
					String pri_port = strs[5];
					String backup_ip = strs[6];
					String backup_port = strs[7];
					
					if(!SessionHandler.bkServer.ip.getHostAddress().equals(pri_ip) || SessionHandler.bkServer.port != Integer.parseInt(pri_port)){
						//won't send back to itself
						//need to modify the conditions
						servers.add(new Server(pri_ip,pri_port));
					}
					if(!SessionHandler.bkServer.ip.getHostAddress().equals(backup_ip) || SessionHandler.bkServer.port != Integer.parseInt(backup_port)){
						//won't send back to itself
						servers.add(new Server(backup_ip,backup_port));
					}
					
					try {
						
						RPCClient.sessionDeleteClient(sessionID, version,servers);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					cookies[i].setMaxAge(0);
					cookies[i].setPath("/");
					response.addCookie(cookies[i]);
					break;
				}
			}
		}
		
	}

	public static void updateCookie(SessionTable sess,HttpServletRequest request,HttpServletResponse response) throws UnknownHostException{
	//	System.out.println("update cookie sessId:" + sess.getSid());
		Server primary = sess.getLocations().get(0);
		Server backup = sess.getLocations().get(1);
		String value = sess.getSid()+"_"+sess.getVersion()+"_"+primary.ip.getHostAddress()+"_"+primary.port+"_"+backup.ip.getHostAddress()+"_"+backup.port;
		Cookie sessionCookie = new Cookie("CS5300PROJ1SESSION", value);
		sessionCookie.setPath("/");
		sessionCookie.setMaxAge(sessionTimeOut+delta);
		response.addCookie(sessionCookie);
		System.out.println(sessionCookie.getValue());
		
	}
	
	public static void writeBackupServer(SessionTable session, Timestamp time){
		if(GroupMembership.isEmpty()){
			ArrayList<Server> servers = new ArrayList<Server>();
			servers.add(localServer);
			servers.add(localServer);
			session.setLocations(servers);
			System.out.println("after adding server: " + session.getLocations().get(0).port);
		}
		else{
			try{
				Server s = null;
				do{
					s = GroupMembership.getServer();
				}while(!RPCClient.sessionWriteClient(session, s, time));
				ArrayList<Server> servers = new ArrayList<Server>();
				servers.add(localServer);
				servers.add(s);
				session.setLocations(servers);
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}
