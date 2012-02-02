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
	serverPort = -1;

	reader = null;
	writer = null;

    } // end constructor

    public static void main(String[] args){

	DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE]);
	boolean wait = false;

	try {
	    socket = new DatagramSocket();
	    wait = true;
	}
	catch (SocketException e){
	    System.out.println("client unable to connect to port " + socket.getPort());
	}


	while (wait){

	    socket.send();

	}

    } // end main method

    private static void ReadMsg(){

    }

    private static void SendMsg(){

    }

} // end class Client