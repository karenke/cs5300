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

import session.SessionHandler;
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
	
	public static SessionTable sessionReadClient(String sessId, int sessVersion, ArrayList<Server> s) throws IOException{
		SessionTable session = null;
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(6000); // Timeout after 2 seconds
		String callID = UUID.randomUUID().toString();//generate unique id for call
		byte[] outBuf = new byte[512];
		//fill outBuf with callId, operationSESSIONREAD, sessionID, sessionVersionNum
		String outStr = callID+"+0+"+sessId+"+"+sessVersion;
		outBuf = string2byte(outStr);
		for(Server e : s){
	//		InetAddress ip = InetAddress.getByName("127.0.0.1");
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, e.ip, e.port);
	//		System.out.println("ip: "+e.ip.getHostAddress());
			System.out.println("port: "+e.port);
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
				String[] strs = response.split("[+]");
				session = new SessionTable(sessId,sessVersion);
				session.setVersion(Integer.parseInt(strs[1]));
				session.setMessage(strs[2]);
				session.setExpiration(Timestamp.valueOf(strs[3]));
				
			}while(response.equals("") || !byte2string(recvPkt.getData()).split("[+]")[0].equals(callID));//the callId in inBuf is not the expected one
		}catch(InterruptedIOException iioe){
			//timeout
			recvPkt = null;
			System.out.println("time out");
		}catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		rpcSocket.close();
		return session;
		
	}

	
	public static boolean sessionWriteClient(SessionTable session,  Server e, Timestamp discard_time) throws IOException{
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(2000); // Timeout after 2 seconds
		String callID = UUID.randomUUID().toString();//generate unique id for call
		byte[] outBuf = new byte[512];
		//fill outBuf with callId, operationSESSIONWrite, sessionID, sessionVersionNum

		String outStr = callID+"+1+"+session.getSid()+"+"+session.getVersion()+"+"+session.getMessage()+"_"+discard_time.toString();
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
			
			}while(!byte2string(recvPkt.getData()).split("[+]")[0].equals(callID));//the callId in inBuf is not the expected one
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
	
	public static boolean sessionDeleteClient(String sessionID, int version,  ArrayList<Server> s) throws IOException{
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(2000); // Timeout after 2 seconds
		String callID = UUID.randomUUID().toString();//generate unique id for call
		byte[] outBuf = new byte[512];
		//fill outBuf with callId, operationSESSIONDelete, sessionID, sessionVersionNum
		String outStr = callID+"+2+"+sessionID+"+"+version;
		outBuf = string2byte(outStr);
		if(s.size() == 0){
			return true;
		}
		for(Server e : s){
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, e.ip, e.port);
			rpcSocket.send(sendPkt);
			System.out.println("packet sent to server");
			
		}
		
		byte[] inBuf = new byte[512];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		try {
			do{
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				System.out.println("packet received from server");
			}while(!byte2string(recvPkt.getData()).equals("ok"));//the callId in inBuf is not the expected one
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
