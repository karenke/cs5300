package session;

import groupMembership.GroupMembership;
import groupMembership.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import RPC.RPCClient;

public class SessionHandler {

	//CurrentHashMap is used for session state table and it is global variable that can be shared among sessions
	public static ConcurrentHashMap<String, SessionTable> global = new ConcurrentHashMap<String, SessionTable>();

	public static int sessionTimeOut = 60;
	
	public static SessionTable getSession(HttpServletRequest request,HttpServletResponse response) throws UnknownHostException{
		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		SessionTable session = null;
		
		int version = 1;
		if(cookies != null){
			for(int i = 0; i < cookies.length; i++){
				if(cookies[i].getName().equals("CS5300PROJ1SESSION")){
					cookie = cookies[i];
					System.out.println("cookie: "+cookie.getValue());
				}
			}
		}
//		String sessionID = request.getSession().getId();
		String sessionID = request.getSession().getId()+"_"+InetAddress.getLocalHost().getHostAddress()+"_"+request.getLocalPort();
		if(cookie == null){//new session, there is no cookie
		//	String sessionID = UUID.randomUUID().toString();
		//  session = new SessionTable(sessionID, GroupMembership.mbrSet);
	//		session = new SessionTable(sessionID,1);
			
		    Timestamp time = new Timestamp(System.currentTimeMillis());
	//		time.setMinutes(time.getMinutes()+5);
		    time.setSeconds(time.getSeconds()+sessionTimeOut);
			session = new SessionTable(sessionID,1,"Welcome, user!",time);
			global.put(sessionID, session);
			
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
				session = global.get(sessionID);
		//		System.out.println(session.sid);
				if(session == null || session.getVersion() != version){
					if(session == null){
						System.out.println("server doesn't have session");
					}
					else{
						System.out.println(session.getVersion() + " does not match with " + version);
					}
				//	session = new SessionTable(sessionID,servers);
					//session is not stored in local server, we should read from others
					
					session = RPCClient.sessionRead(sessionID, version, servers);
					session.setVersion(version+1);
					global.put(sessionID, session);
				}
				else{
					session.setLocations(servers);
					version = global.get(sessionID).getVersion()+1;
					global.get(sessionID).setVersion(version);
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
		time.setSeconds(time.getSeconds()+sessionTimeOut);
		global.get(sessionID).setExpiration(time);	
	//	String location = request.getLocalAddr()+": "+ request.getServerPort();
		String location = InetAddress.getLocalHost().getHostAddress() + ": " + request.getServerPort();
	//	System.out.println(InetAddress.getLocalHost().getHostAddress());
		Timestamp expr = global.get(sessionID).getExpiration();
		String msg = global.get(sessionID).getMessage();
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
		time.setSeconds(time.getSeconds()+sessionTimeOut);
		global.get(sessionID).setExpiration(time);
		global.get(sessionID).setMessage(newMsg);
		
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
		global.remove(sessionID);//remove the session from table
		Cookie[] cookies = request.getCookies();
		Cookie cookie = null;
		
		if(cookies != null){
			for(int i = 0; i < cookies.length; i++){
				if(cookies[i].getName().equals("CS5300PROJ1SESSION")){
					cookies[i].setMaxAge(0);
					cookies[i].setPath("/");
					response.addCookie(cookies[i]);
					break;

				}
			}
		}
	}
	
	public static void updateCookie(SessionTable sess,HttpServletRequest request,HttpServletResponse response) throws UnknownHostException{
	//	Server backup = GroupMembership.getServer();
	//	System.out.println("update cookie sessId:" + sess.getSid());
		Server backup = new Server(InetAddress.getLocalHost(),request.getLocalPort());
//		String value = sess.getSid()+"_"+InetAddress.getLocalHost().getHostAddress()+"_"+request.getLocalPort()+"_"+sess.getVersion()+"_"+InetAddress.getLocalHost().getHostAddress()+"_"+request.getLocalPort()+"_"+backup.ip.getHostAddress()+"_"+backup.port;
		String value = sess.getSid()+"_"+sess.getVersion()+"_"+InetAddress.getLocalHost().getHostAddress()+"_"+request.getLocalPort()+"_"+backup.ip.getHostAddress()+"_"+backup.port;
		Cookie sessionCookie = new Cookie("CS5300PROJ1SESSION", value);
		sessionCookie.setPath("/");
		sessionCookie.setMaxAge(sessionTimeOut);
		response.addCookie(sessionCookie);
		
	}
	
}
