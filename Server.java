import java.io.*;
import java.net.*;

/**
 * @desc This the server process that communicates
 *       with some client.
 */
public class Server
{

//    private ConfigFile config;
    private static DatagramSocket socket;
    private static int port;

    private static int PACKET_SIZE = 2048;

    public Server(){

	socket = null;
	port = -1;

    } // end constructor


    public static void main(String[] args){

	DatagramPacket packetOut = null;
	DatagramPacket packetIn = null;
	InetAddress address = null;

	byte[] dataOut = new byte[PACKET_SIZE];
	byte[] dataIn = new byte[PACKET_SIZE];

	IPAddress client = null;
	int clientPort = -1;
	boolean wait = false;

	port = 1648;

	try {

	    socket = new DatagramSocket(port);
	    wait = true;

	    System.out.println("server located at IP " +
//			       socket.getInetAddress().getHostAddress() +
			       " and port " + port);

	    packetIn = new DatagramPacket(dataIn, dataIn.length);

	}
	catch (SocketException e) {
	    System.out.println("server could not open UDP port on port "
			       + port);
	}

	String response = null;
	String message = null;

	int counter = 0;

try{
	while (wait){

	    socket.receive(packetIn);	    

	    address = packetIn.getAddress();
	    clientPort = packetIn.getPort();

	    message = new String(packetIn.getData());

	    if (counter == 100){
		response = "die";
		wait = false;
	    }
	    else {
		response = "42";
	    }

	    dataOut = response.getBytes();
	    packetOut = new DatagramPacket(dataOut, dataOut.length,
					   address, clientPort);

	    socket.send(packetOut);

	    counter++;

	} // end while wait
}
catch (Exception e){ }


	try {
	    socket.close();
	}
	catch (Exception e){ }

    } // end main

} // end class Server