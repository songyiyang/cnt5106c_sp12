import java.io.*;
import java.net.*;

/**
 * @desc This the client process that communicates
 *       with the server.
 */
public class Client
{

    private static IPAddress server = null;
    private static DatagramSocket socket = null;
    private static BufferedReader in = null;


    public static void main(String[] args){

	DatagramPacket packetIn = null;
	byte[] dataIn = null;

	boolean wait = false;

	    // First, establish a socket and set up the
	    // server's IP address.
	try {
	    socket = new DatagramSocket();
	    wait = true;
	}
	catch (SocketException e){
	    System.exit(1);
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
	IPAddress server = null;

	    // Loop forever until shutdown signal sent
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
	    system_msg = ClientProtocol.parseCommand(input, socket);

		// If user wants to close the client, do so
	    if (system_msg.equals("quit")){
		break;
	    }

		// If user set variables for server, loop back
		// around so that s/he can begin sending commands
	    else if (system_msg.matches("set.*")){

		if (system_msg.matches("set .+")){
		    String[] address = system_msg.split("\\s");
		    String[] ip = address[1].split(":");

		    InetAddress inetAddress = null;

		    try {
			inetAddress = InetAddress.getByName(ip[0]);
		    }
		    catch (UnknownHostException e){
			// do nothing, it's been verified already
		    }

		    int port = Integer.parseInt(ip[1]);

		    server = new IPAddress(inetAddress, port);
		}

		continue;
	    }

		// Program couldn't understand the output.
		// Re-ask user for input.
	    else if (system_msg.equals("WTF")){
		System.out.println("wtf were you doing? try to enter " +
				   "the command again.");
		continue;
	    }

	    else if (server == null){
		System.out.println("must set server IP and port!");
		continue;
	    }

		// Send the packet to the server
	    ClientProtocol.send(socket, system_msg, server);

		// Get the response
	    server = ClientProtocol.receive(socket, packetIn);

		// Finally parse the response
	    system_msg = ClientProtocol.parseResponse(socket, packetIn, server);

		// If system gives the signal, shut program down
	    if (system_msg.equals("game over")){
		wait = false;
	    }

	} // end while true

	    // Close the socket
	try {
	    socket.close();
	}
	catch (Exception e){
	    e.printStackTrace();
	}

    } // end main method

} // end class Client