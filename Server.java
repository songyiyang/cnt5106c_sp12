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



    public Server(){

	socket = null;
	port = -1;

    } // end constructor


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

	String response = null;
	String message = null;

	int counter = 0;

	while (wait){

	    System.out.println(message);

	    client = Protocol.receive(socket, packetIn);

	    message = new String(packetIn.getData());

	    if (counter == 100){
		response = "die";
		wait = false;
	    }
	    else {
		response = "42";
	    }

	    Protocol.send(socket, response, client);

	    counter++;

	} // end while wait


	try {
	    socket.close();
	}
	catch (Exception e){ }

    } // end main

} // end class Server