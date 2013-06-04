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
import java.net.SocketException;
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
	
	@SuppressWarnings("finally")
	public DatagramPacket sessionRead(String sessId, int sessVersion, ArrayList<Server> s) throws IOException{
		
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
		}
		
		byte[] inBuf = new byte[512];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		try {
			do{
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
			
			}while(!byte2string(recvPkt.getData()).split("[_]")[0].equals(callID));//the callId in inBuf is not the expected one
		}catch(InterruptedIOException iioe){
			//timeout
			recvPkt = null;
		}catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		finally{
			rpcSocket.close();
			return recvPkt;
		}
	}

	public static DatagramPacket sessionRead(String sessId, int sessVersion) throws IOException{
		
		DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(2000); // Timeout after 2 seconds
		String callID = UUID.randomUUID().toString();//generate unique id for call
		byte[] outBuf = new byte[512];
		//fill outBuf with callId, operationSESSIONREAD, sessionID, sessionVersionNum
		String outStr = callID+",0,"+sessId+","+sessVersion;
		outBuf = string2byte(outStr);
//		for(Server e : s){
//			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, e.ip, e.port);
//			rpcSocket.send(sendPkt);
//		}
		
		byte[] inBuf = new byte[512];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		try {
			do{
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
			}while(!byte2string(recvPkt.getData()).split("[,]")[0].equals(callID));//the callId in inBuf is not the expected one
		}catch(InterruptedIOException iioe){
			//timeout
			recvPkt = null;
		}catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		finally{
			rpcSocket.close();
			return recvPkt;
		}
	}
	
	
	public static DatagramPacket sessionWrite(String sessId, int sessVersion){
		return null;
	}
/**
 * Find session data from SSM servers, return null if not found
 * 
 * @return
 */
	public static void getSess(SessionTable s){
		try {
		    DatagramSocket rpcSocket = new DatagramSocket();
		    rpcSocket.setSoTimeout(2000); // Timeout after 2 seconds
		    String callID = UUID.randomUUID().toString();
		    String outstr = (callID + ",1," + s.getSid() + "," + s.getVersion());
		    
		
		    byte[] outBuf = string2byte(outstr);
		    System.out.println("Get call sending: " + outstr);
		    for (Server e : s.getLocations()) {
		    	DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, e.ip, e.port);
		    	try {
		    		rpcSocket.send(sendPkt);
			    } catch (IOException e1) {
			       // TODO Auto-generated catch block
			    	e1.printStackTrace();
			    }
		    }
		    byte[] inBuf = new byte[4096];
		    DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		    String response_str = null;
		    recvPkt.setLength(inBuf.length);
		    do {
		    	try {
		    		rpcSocket.receive(recvPkt);
			      response_str = byte2string(inBuf);
			
		    	} catch (IOException e1) {
		    		e1.printStackTrace();
		//	        return null;
		    	} 
		    } while (response_str == null || response_str.equals("") || !(response_str.split(",")[0].equals(callID)));
	
		    System.out.println("Client received response: " + response_str);
		    String[] response = response_str.split(",");
//	    s.setData("count", response[1]);
//	    s.setData("message", URLDecoder.decode(response[2],"UTF-8"));
	
	  } catch (SocketException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
//	    return null;
	  }
		
	}
	
	
}
