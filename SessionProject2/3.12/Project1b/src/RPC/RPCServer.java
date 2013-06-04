package RPC;

import groupMembership.GroupMembership;
import groupMembership.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Timestamp;

import session.SessionHandler;
import session.SessionTable;



public class RPCServer extends Thread {

	DatagramSocket rpcSocket;
	int serverPort;
	public static boolean isCrash;
	  
	public RPCServer() {
		System.out.println("RPC Server started...");
		try {
			rpcSocket = new DatagramSocket();
			serverPort = rpcSocket.getLocalPort();
	//		System.out.println("RPC Server port: "+ serverPort);
			isCrash = false;
		} catch (SocketException e) {
			// TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	  
	
	public static void setCrash(boolean b){
		isCrash = b;
	}
	public int getServerPort() {
		return serverPort;
	}

	public void run() {
//		while(true){
		while(!isCrash){
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
	       
	            int port = 0;
	            SessionTable session = null;
            
	            byte[] outBuf = null;
	            switch(operationCode){
	            	case 0://sessionRead
	            		port = Integer.parseInt(request[4]);
						session = sessionRead(sessId,sessVersion);
						if( session != null){
							response = callid+"+"+session.getVersion()+"+"+session.getMessage()+"+"+session.getExpiration().toString();
							response += "+"+session.getLocations().get(0).ip.getHostAddress()+"+"+session.getLocations().get(0).port;
							response += "+"+session.getLocations().get(1).ip.getHostAddress()+"+"+session.getLocations().get(1).port;
						}
						
	            		break;
	            	case 1://sessionWrite
						String message = request[4];
						String discard_time = request[5];
						port = Integer.parseInt(request[6]);
						if(sessionWrite(sessId, sessVersion, message,Timestamp.valueOf(discard_time)) == true){
							response = callid;
						}
						else{
							response = "no";
						}
	            		break;
	            	case 2://sessionDelete
	            		if(sessionDelete(sessId,sessVersion)){
	            			response = "ok";
	            		}
	            		else{
	            			response = "no";
	            		}
	            		break;
	            }
	            
	            outBuf = RPCClient.string2byte(response);
	            // here outBuf should contain the callID
	            DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
				rpcSocket.send(sendPkt);
				System.out.println("packet send to client");
				
				System.out.println("port: "+port);
				//an IPP is inserted into the mbrSet whenever a RPC request is received
				GroupMembership.CheckUpdate(new Server(returnAddr,port), true);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(isCrash){
			System.out.println("RPCServer crash, it cannot responding to RPCs and HTTP request");
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
	
