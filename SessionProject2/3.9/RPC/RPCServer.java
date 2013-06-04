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

import session.SessionServlet;
import session.SessionTable;



public class RPCServer extends Thread {

	DatagramSocket rpcSocket;
	int serverPort;
	  
	public RPCServer() {
		try {
			rpcSocket = new DatagramSocket();
			serverPort = rpcSocket.getLocalPort();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
	        e.printStackTrace();
	    }
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
	            String[] request = RPCClient.byte2string(inBuf).split("[_]");
	            
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
						session = SessionServlet.global.get(sessId);
						if(session != null && session.getVersion() == sessVersion){
							response = callid+"_"+session.getVersion()+"_"+session.getMessage()+"_"+session.getExpiration().toString();
							
						}
						//	outBuf = RPCClient.sessionRead(sessId,sessVersion).getData();
	            		break;
	            	case 1://sessionWrite
	            		String message = null;
						String discard_time = null;
						message = request[4];
						discard_time = request[5];
	
						session = new SessionTable(sessId, sessVersion, message,Timestamp.valueOf(discard_time));
						SessionServlet.global.put(sessId, session);
						response = callid;
	            //		outBuf = RPCClient.sessionWrite(sessId, sessVersion).getData();
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
}
	
