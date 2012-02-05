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

    private static BufferedReader in;

    public Client(){

	server = null;
	socket = null;

	in = null;

    } // end constructor

    public static void main(String[] args){

	DatagramPacket packetIn = null;
	byte[] dataIn = null;
	int port = 1648;

	boolean wait = false;

	    // First, establish a socket and set up the
	    // server's IP address.
	try {
	    socket = new DatagramSocket();
	    wait = true;

	    server = new IPAddress(InetAddress.getByName("nicka-linux"),
				   port);
	}
	catch (SocketException e){
	    System.out.println("client unable to connect to port "
			       + port);
	}
	catch (UnknownHostException e){
	    e.printStackTrace();
	}


	    // Now set up a reader in order to receive
	    // user input. Exit program if unable to get
	    // stream.
	try {
	    in = new BufferedReader(new InputStreamReader(System.in));
	}
	catch (Exception e){
	    System.out.println("unable to get user input, " +
			       "shutting down client.");
	    System.exit(1);
	}

	String input = "";
	String system_msg = null;

	String prompt = "$ ";

	while (wait){

		// Create new, empty packet
	    dataIn = new byte[Protocol.PACKET_SIZE];
	    packetIn = new DatagramPacket(dataIn, dataIn.length);

		// Prompt user for input
	    System.out.print(prompt);

	    try {
		input = in.readLine();
	    }
	    catch (IOException e){
		continue;
	    }

		// Parse the input and return a system message
	    system_msg = ClientProtocol.parseCommand(input);

		// If user wants to close the client, do so
	    if (system_msg.equals("quit")){
		break;
	    }
	    else if (system_msg.equals("WTF")){
		continue;
	    }

		// Send the packet to the server
	    ClientProtocol.send(socket, system_msg, server);

		// Get the response
	    server = ClientProtocol.receive(socket, packetIn);

		// Finally parse the response
	    system_msg = ClientProtocol.parseResponse(packetIn, server);

	    if (system_msg.equals("game over")){
		wait = false;
	    }

	} // end while true

	try {
	    socket.close();
	}
	catch (Exception e){
	    e.printStackTrace();
	}

    } // end main method

} // end class Client