

import groupMembership.GroupMembership;
import groupMembership.Server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
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

import RPC.RPCServer;

import session.SessionHandler;
import session.SessionTable;

/**
 * Servlet implementation class SessionServlet
 */
@WebServlet("/SessionServlet")
public class SessionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	//CurrentHashMap is used for session state table and it is global variable that can be shared among sessions
	public static ConcurrentHashMap<String, SessionTable> global = new ConcurrentHashMap<String, SessionTable>();

	public static RPCServer rpcServer = new RPCServer();
	 
	public static GroupMembership gm; 
	/**
     * @see HttpServlet#HttpServlet()
     */
    public SessionServlet() {
        super();
        // TODO Auto-generated constructor stub
       
 //       System.out.println(rpcServer.getServerPort());
        rpcServer.start();
        try {
        	gm = new GroupMembership();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//        try {
//			new GroupMembership().start();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        
        try {
        	SessionHandler.localServer = new Server(InetAddress.getLocalHost(), rpcServer.getServerPort());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		
		global = SessionHandler.global;
		
		SessionTable session = SessionHandler.getSession(request, response);
//		System.out.println("session servlet session id: "+ session.getSid());
		HttpSession sess = request.getSession();
		
		
		if (command.equals("Refresh")) {
			sess = SessionHandler.refresh(request, session);
				
			String location = sess.getAttribute("location").toString();
			String expr = sess.getAttribute("exp").toString();
			String msg = sess.getAttribute("msg").toString();
				
			SessionHandler.updateCookie(session, request, response);
			
			out.println(location);
			out.println(expr.toString());
			out.println(msg);	
			out.flush();
			out.close();

		}
		else if (command.equals("Replace")) {
			sess = SessionHandler.replace(request, session);
			String location = sess.getAttribute("location").toString();
			String expr = sess.getAttribute("exp").toString();
			String msg = sess.getAttribute("msg").toString();
				
			SessionHandler.updateCookie(session, request, response);
			
			out.println(location);
			out.println(expr.toString());
			out.println(msg);
			out.flush();
			out.close();
		}
		else if (command.equals("Logout")){
			sess.invalidate();
			SessionHandler.removeSession(session, request, response);
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			out.println("<HTML>");
			out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
			out.println("  <BODY>");
			out.print("<h1> You have successfully logged out! </h1>");
			out.println("  </BODY>");
			out.println("</HTML>");
			out.flush();
			out.close();
		//	return;
		}
			
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	

	private ConcurrentHashMap<String, SessionTable> findTableStoringSessions(){
		return SessionServlet.global;
	}
	
}
