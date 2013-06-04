package RPC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;



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
	   
	public int getPort() {
      return this.serverPort;
	}
	
	public void run() {
		while(true){
			byte[] inBuf = new byte[512];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
			try {
				rpcSocket.receive(recvPkt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
       
            byte[] outBuf = null;
            switch(operationCode){
            	case 0://sessionRead
					try {
						outBuf = RPCClient.sessionRead(sessId,sessVersion).getData();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            		break;
            	case 1://sessionWrite
            		outBuf = RPCClient.sessionWrite(sessId, sessVersion).getData();
            		break;
            }
            // here outBuf should contain the callID
            DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
            try {
				rpcSocket.send(sendPkt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	     
	}
}
	
