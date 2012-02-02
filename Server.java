import java.io.*;
import java.net.*;

/**
 * @desc This the server process that communicates
 *       with some client.
 */
public class Server
{

//    private ConfigFile config;
    private DatagramSocket socket;
    private int port;

    private static int PACKET_SIZE = 2048;

    public Server(){

	socket = null;
	port = -1;

    } // end constructor


    public static void main(String[] args){

	IPAddress client = null;
	DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
	boolean wait = false;

	port = 1648;

	try {
	    socket = new DatagramSocket(port);
	    wait = true;
	    System.out.println("server located at IP " +
			       socket.getInetAddress().getHostAddress() +
			       " and port " + port);
	}
	catch (SocketException e) {
	    System.out.println("server could not open UDP port on port " + port);
	}

	while (wait){

	    socket.receive(packet);

	} // end while wait

    } // end main

} // end class Server