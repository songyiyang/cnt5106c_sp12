import java.io.*;
import java.net.*;
import java.util.TreeMap;

/**
 * @desc This the server process that communicates
 *       with some client.
 */
public class Server
{

//    private ConfigFile config;
    private static DatagramSocket socket;
    private static int port;

    private static TreeMap<String,IPAddress> records =
		   new TreeMap<String,IPAddress>();


    public static void main(String[] args){

	DatagramPacket packetIn = null;
	byte[] dataIn = new byte[Protocol.PACKET_SIZE];

	IPAddress client = null;

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

	String system_msg = null;

	while (wait){

	    dataIn = new byte[Protocol.PACKET_SIZE];
	    packetIn = new DatagramPacket(dataIn, dataIn.length);

	    client = ServerProtocol.receive(socket, packetIn);
	    system_msg = ServerProtocol.parseMessage(socket, packetIn, client);

	    if (system_msg.matches("^.+ GAMEOVER$")){
		wait = false;
	    }

	} // end while wait


	try {
	    socket.close();
	}
	catch (Exception e){ }

    } // end main

} // end class Server