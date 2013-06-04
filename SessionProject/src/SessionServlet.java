

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class SessionServlet
 */
@WebServlet("/SessionServlet")
public class SessionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	//CurrentHashMap is used for session state table and it is global variable that can be shared among sessions
	public static ConcurrentHashMap<String, SessionTable> global = new ConcurrentHashMap<String, SessionTable>();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SessionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("deprecation")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=UTF-8");
		String command = request.getParameter("cmd");
		PrintWriter out = response.getWriter();
		HttpSession sess = request.getSession(true);

		ConcurrentHashMap<String, SessionTable> globalTable = findTableStoringSessions();
		Integer version = 1;
		//synchronize the session table
		synchronized(globalTable){
			String sessionID = sess.getId();
			System.out.println(sessionID);
			String locationMeta = "";
			for(int i = 0; i < request.getCookies().length; i++){
				if(request.getCookies()[i].getName().equals("CS5300PROJ1SESSION")){
					String content = request.getCookies()[i].getValue();
					String[] strs = content.split("[_]");
					sessionID = strs[0];
					version = Integer.parseInt(strs[1]);
					locationMeta = strs[2];
				}
//				System.out.println("cookie "+i+ " " + request.getCookies()[i].getName()+ ": "+request.getCookies()[i].getValue());
			}
//			System.out.println(sessionID+" "+ version + " " + locationMeta);

			if(!globalTable.containsKey(sessionID)){
				Timestamp time = new Timestamp(System.currentTimeMillis());
				time.setMinutes(time.getMinutes()+5);
				SessionTable table = new SessionTable(1,"Welcome, user!",time);
				globalTable.put(sessionID, table);
			}
			else{
				version = globalTable.get(sessionID).getVersion()+1;
				globalTable.get(sessionID).setVersion(version);
			}
//			System.out.println(sess.getId());	
//			Iterator<String> itr = globalTable.keySet().iterator();
//			while(itr.hasNext()){
//				System.out.println("table key: " + itr.next());
//			}
						
			String value = sessionID+"_"+version+"_"+InetAddress.getLocalHost().getHostAddress();
			Cookie sessionCookie = new Cookie("CS5300PROJ1SESSION", value);
			sessionCookie.setPath("/");
			response.addCookie(sessionCookie);
		
			if (command.equals("Refresh")) {
				Timestamp time = new Timestamp(System.currentTimeMillis());
				time.setMinutes(time.getMinutes()+5);
				globalTable.get(sessionID).setExpiration(time);	
			//	String location = request.getLocalAddr()+": "+ request.getServerPort();
				String location = InetAddress.getLocalHost().getHostAddress() + ": " + request.getServerPort();
			//	System.out.println(InetAddress.getLocalHost().getHostAddress());
				Timestamp expr = globalTable.get(sessionID).getExpiration();
				String msg = globalTable.get(sessionID).getMessage();
				sess.setAttribute("msg", msg);
				sess.setAttribute("exp", expr);
				sess.setAttribute("location", location);
				
				out.println(location);
				out.println(expr.toString());
				out.println(msg);
				out.flush();
				out.close();
			}
			else if (command.equals("Replace")) {
				String newMsg = request.getParameter("newMsg");
				System.out.println(newMsg);
				if(newMsg.length() <= 240) {//limit the size of msg in order to fit UDP package
					Timestamp time = new Timestamp(System.currentTimeMillis());
					time.setMinutes(time.getMinutes()+5);
					globalTable.get(sessionID).setExpiration(time);	
					globalTable.get(sessionID).setMessage(newMsg);		
					String location = InetAddress.getLocalHost().getHostAddress() + ": " + request.getServerPort();
					Timestamp expr = globalTable.get(sessionID).getExpiration();
					String msg = globalTable.get(sessionID).getMessage();
					
					sess.setAttribute("msg", msg);
					sess.setAttribute("exp", expr);
					sess.setAttribute("location", location);
					
					out.println(location);
					out.println(expr.toString());
					out.println(msg);
					out.flush();
					out.close();
				}
				else {
					out.println("Message too long!!");
					out.flush();
					out.close();
				}
			}
			else if (command.equals("Logout")){
				sess.invalidate();//invalidate the session
				globalTable.remove(sessionID);//remove the session from table
				sessionCookie.setMaxAge(0);
				response.addCookie(sessionCookie);
				
				out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
				out.println("<HTML>");
				out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
				out.println("  <BODY>");
				out.print("<h1> You have successfully logged out! </h1>");
				out.println("  </BODY>");
				out.println("</HTML>");
				out.flush();
				out.close();
			}
			

		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	private String makeUniqueString(String serverName, String sessId){
		UUID id = new UUID(Long.parseLong(serverName),Long.parseLong(sessId));
		return id.toString();
	}

	private ConcurrentHashMap<String, SessionTable> findTableStoringSessions(){
		return SessionServlet.global;
	}
}
