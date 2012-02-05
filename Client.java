import java.io.*;
import java.net.*;

/**
 * @desc This the client process that communicates
 *       with the server.
 */
public class Client
{

    private static IPAddress server;
    private static DatagramSocket socket;

    private static BufferedReader reader;
    private static PrintWriter writer;

    private static int PACKET_SIZE = 2048;

    public Client(){

	server = null;
	socket = null;

	reader = null;
	writer = null;

    } // end constructor

    public static void main(String[] args){

	DatagramPacket packetIn = null;
	int port = 1648;

	boolean wait = false;

	byte[] dataIn = new byte[PACKET_SIZE];

	try {
	    socket = new DatagramSocket();
	    wait = true;

	    server = new IPAddress(InetAddress.getByName("nicka-linux"),
				   port);
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

	while (wait){

	    System.out.println(message);
	    Protocol.send(socket, message, server);

	    server = Protocol.receive(socket, packetIn);

	    response = new String(packetIn.getData());
	    response = response.trim();

	    if (response.matches("die")){
		wait = false;
	    }

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