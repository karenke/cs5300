
import java.net.InetAddress;
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
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;



public class RPCClient {
    
    public static void main(String[] args) throws IOException{
                
        DatagramSocket rpcSocket = new DatagramSocket();
		rpcSocket.setSoTimeout(2000); // Timeout after 2 seconds
		String callID = UUID.randomUUID().toString();//generate unique id for call
		byte[] outBuf = new byte[512];
        outBuf[0] = 0;
        outBuf[1] = 1;
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, ip, 4445);
        rpcSocket.send(sendPkt);
        System.out.println("packet sent to server");
	//	}
		
		byte[] inBuf = new byte[512];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		String response = "";
        
//		try {
//			do{
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				System.out.println("packet received from server");
                String received = new String(recvPkt.getData(), 0, recvPkt.getLength());
                System.out.println("Quote of the Moment: " + received);

     //           System.out.println("response from server: "+response + ",data: "+recvPkt.getData());
				
//			}while(response.equals(""));//the callId in inBuf is not the expected one
//		}catch(InterruptedIOException iioe){
//			//timeout
//			recvPkt = null;
//            System.out.println("time out");
//		}catch (SocketException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//            System.out.println("connection error");
//		}
//		
		rpcSocket.close();
    }
	
	
}
