package RPC;



import groupMembership.Server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import session.SessionTable;


public class RPCClient {

	public static byte[] string2byte(String data) {
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(data);
			byte[] output = bos.toByteArray();
			return output;
	    } catch (IOException e) {
	      // TODO Auto-generated catch block
	    	e.printStackTrace();
	    	return null;
	    }
	}
	
	
	public static String byte2string(byte[] data) {

		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInput in = new ObjectInputStream(bis);
			String output = (String) in.readObject();
			return output;
	    } catch (IOException e) {
	      // TODO Auto-generated catch block
	    	e.printStackTrace();
	    	return null;
	    } catch (ClassNotFoundException e) {
	      // TODO Auto-generated catch block
	    	e.printStackTrace();
	    	return null;
	    }
	}
	
	public static SessionTable sessionRead(String sessId, int sessVersion, ArrayList<Server> s) throws IOException{
		SessionTable session = null;
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(2000); // Timeout after 2 seconds
		String callID = UUID.randomUUID().toString();//generate unique id for call
		byte[] outBuf = new byte[512];
		//fill outBuf with callId, operationSESSIONREAD, sessionID, sessionVersionNum
		String outStr = callID+"_0_"+sessId+"_"+sessVersion;
		outBuf = string2byte(outStr);
		for(Server e : s){
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, e.ip, e.port);
			rpcSocket.send(sendPkt);
			System.out.println("packet sent to server");
		}
		
		byte[] inBuf = new byte[512];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		String response = "";
		try {
			do{
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				System.out.println("packet received from server");
				response = byte2string(inBuf);
				String[] strs = response.split("[_]");
				session = new SessionTable(sessId,sessVersion);
				session.setVersion(Integer.parseInt(strs[1]));
				session.setMessage(strs[2]);
				session.setExpiration(Timestamp.valueOf(strs[3]));
				
			}while(response.equals("") || !byte2string(recvPkt.getData()).split("[_]")[0].equals(callID));//the callId in inBuf is not the expected one
		}catch(InterruptedIOException iioe){
			//timeout
			recvPkt = null;
		}catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		rpcSocket.close();
		return session;
		
	}

	
	public static boolean sessionWrite(SessionTable session,  Server e) throws IOException{
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(2000); // Timeout after 2 seconds
		String callID = UUID.randomUUID().toString();//generate unique id for call
		byte[] outBuf = new byte[512];
		//fill outBuf with callId, operationSESSIONWrite, sessionID, sessionVersionNum
		String outStr = callID+"_1_"+session.getSid()+"_"+session.getVersion()+"_"+session.getMessage()+"_"+session.getExpiration().toString();
		outBuf = string2byte(outStr);
	//	for(Server e : s){
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, e.ip, e.port);
			rpcSocket.send(sendPkt);
	//	}
		
		byte[] inBuf = new byte[512];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		try {
			do{
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
			
			}while(!byte2string(recvPkt.getData()).split("[_]")[0].equals(callID));//the callId in inBuf is not the expected one
		}catch(InterruptedIOException iioe){
			//timeout
			iioe.printStackTrace();
			return false;
		}catch (SocketException e0) {
			// TODO Auto-generated catch block
			e0.printStackTrace();
			return false;
		}	
	
		rpcSocket.close();
		return true;
	}

	
}
