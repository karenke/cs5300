package RPC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Timestamp;

import session.SessionHandler;
import session.SessionTable;



public class RPCServer extends Thread {

	DatagramSocket rpcSocket;
	int serverPort;
	  
	public RPCServer() {
		System.out.println("RPC Server started...");
		try {
			rpcSocket = new DatagramSocket();
			serverPort = rpcSocket.getLocalPort();
	//		System.out.println("RPC ip: "+rpcSocket.getLocalAddress().getHostAddress());
			System.out.println("RPC Server port: "+ serverPort);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	  
	
	public int getServerPort() {
		return serverPort;
	}

	public void run() {
		while(true){
			try {
				byte[] inBuf = new byte[512];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				rpcSocket.receive(recvPkt);
				System.out.println("packet received from client");
            
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
	            // here inBuf contains the callID and operationCode
	            String[] request = RPCClient.byte2string(inBuf).split("[+]");
	            
	            if (request.length < 4){
	            	return;
	            }
	            String callid = request[0];
	            int operationCode = Integer.parseInt(request[1]);
	            String sessId = request[2];
	            int sessVersion = Integer.parseInt(request[3]);
	            String response = null;
	       
	            SessionTable session = null;
            
	            byte[] outBuf = null;
	            switch(operationCode){
	            	case 0://sessionRead
						session = sessionRead(sessId,sessVersion);
						if( session != null){
							response = callid+"+"+session.getVersion()+"+"+session.getMessage()+"+"+session.getExpiration().toString();
						}
	            		break;
	            	case 1://sessionWrite
						String message = request[4];
						String discard_time = request[5];
						if(sessionWrite(sessId, sessVersion, message,Timestamp.valueOf(discard_time)) == true){
							response = callid;
						}
	            		break;
	            	case 2://sessionDelete
	            		if(sessionDelete(sessId,sessVersion)){
	            			response = "ok";
	            		}
	            		break;
	            }
	            
	            outBuf = RPCClient.string2byte(response);
	            // here outBuf should contain the callID
	            DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
				rpcSocket.send(sendPkt);
				System.out.println("packet send to client");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	     
	}
	
	public static SessionTable sessionRead(String sessionID, int version){
		SessionTable session = SessionHandler.global.get(sessionID);
		if(session == null || session.getVersion() < version){//not found
			if(session == null){
				System.out.println("server doesn't have session");
			}
			else{
				System.out.println(session.getVersion() + " does not match with " + version);
			}
			return null;
		}
		
		return session;
	}
	
	public static boolean sessionWrite(String sessionID, int version, String data, Timestamp discard_time){
		try{
			SessionTable session = new SessionTable(sessionID, version, data,discard_time);
			SessionHandler.global.put(sessionID, session);
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
		
	}
	
	public static boolean sessionDelete(String sessionID, int version){
		SessionTable session = SessionHandler.global.get(sessionID);
		if(session != null && session.getVersion() <= version){//not found
			SessionHandler.global.remove(sessionID);
			System.out.println("session removed successfully");
			return true;
		}
		return false;
	}
}
	
