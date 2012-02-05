import java.io.*;
import java.net.*;

/**
 * @desc This the client process that communicates
 *       with the server.
 */
public class Client
{

    private static IPAddress serverIP;
    private static DatagramSocket socket;
    private static int port;

    private static BufferedReader reader;
    private static PrintWriter writer;

    private static int PACKET_SIZE = 2048;

    public Client(){

	serverIP = null;
	socket = null;
	port = -1;

	reader = null;
	writer = null;

    } // end constructor

    public static void main(String[] args){

	DatagramPacket packetOut = null;
	DatagramPacket packetIn = null;
	InetAddress address = null;
	port = 1648;

	boolean wait = false;

	byte[] dataOut = new byte[PACKET_SIZE];
	byte[] dataIn = new byte[PACKET_SIZE];

	try {
	    socket = new DatagramSocket();
	    wait = true;

	    address = InetAddress.getByName("nicka-linux");
	    packetIn = new DatagramPacket(dataIn, dataIn.length);
	}
	catch (SocketException e){
	    System.out.println("client unable to connect to port "
			       + socket.getPort());
	}
	catch (UnknownHostException e){
	    e.printStackTrace();
	}


	String message = "Ping me!";
	String response = null;


try {
	while (wait){

	    dataOut = message.getBytes();
	    packetOut = new DatagramPacket(dataOut, dataOut.length,
					   address, port);

	    System.out.println(message);
	    socket.send(packetOut);

	    socket.receive(packetIn);
	    response = new String(packetIn.getData());
	    response = response.trim();

	    if (response.matches("die")){
		wait = false;
	    }

	}
}
catch (Exception e){

}

	try {
	    socket.close();
	}
	catch (Exception e){
	    e.printStackTrace();
	}

    } // end main method

    private static byte[] ReadMsg(){
	return new byte[PACKET_SIZE];
    }

    private static void SendMsg(byte[] msg){

    }

} // end class Client