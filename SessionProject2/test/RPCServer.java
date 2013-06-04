
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Timestamp;


public class RPCServer extends Thread {

    DatagramSocket rpcSocket;
	int serverPort;
    
	  
	public RPCServer() {
		try {
			this.rpcSocket = new DatagramSocket(4445);
			this.serverPort = rpcSocket.getLocalPort();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	public void run() {
		while(true){
            try {
      //      System.out.println("hi");
			byte[] inBuf = new byte[512];
			DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
            rpcSocket.receive(recvPkt);
            System.out.println("packet received from client");
			
            
            InetAddress returnAddr = recvPkt.getAddress();
            int returnPort = recvPkt.getPort();
            String response = null;
       
                        
            byte[] outBuf = new byte[512];

            
            response = "hi";
            outBuf = response.getBytes();
            
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
	
