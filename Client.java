import java.io.*;
import java.net.*;
import java.util.LinkedList;

/**
 * @desc This the client process that communicates
 *       with the server.
 */
public class Client
{

    private static IPAddress server = null;
    private static BufferedReader in = null;

    private static LinkedList<String> mailbox = new LinkedList<String>();
    private static LinkedList<MailDaemon> daemons =
		   new LinkedList<MailDaemon>();


    public static void main(String[] args){

	DatagramPacket packetIn = null;
	byte[] dataIn = null;

	boolean wait = false;

	    // First, establish a socket and set up the
	    // server's IP address.
	try {
	    ClientProtocol.socket = new DatagramSocket();
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

	    // Print inital message to user
	System.out.println("\nWelcome to client application. Available \n" +
			   "commands are insert, delete, find, server, \n" +
			   "kill, and quit. Please refer to the \n" +
			   "DOCUMENTATION_P1.txt file for information on \n" +
			   "how to use these commands.\n");

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
	    system_msg = ClientProtocol.parseCommand(input);

		// If user wants to close the client, do so
	    if (system_msg.equals("quit")){
		break;
	    }

		// Program couldn't understand the output.
		// Re-ask user for input.
	    else if (system_msg.equals("WTF")){
		System.out.println("command not understood");
		continue;
	    }

	    else if (system_msg.matches(".*TEST.*")){

		String testMsg[] = system_msg.split("\\s");
		String ipPortion[] = testMsg[testMsg.length-1].split(":");

		server = new IPAddress(ipPortion[0],
				       Integer.parseInt(ipPortion[1]));
	    }

	    else if (server == null){
		System.out.println("must set server IP and port!");
		continue;
	    }

		// Send the packet to the server
	    ClientProtocol.send(system_msg, server);

		// Get the response
	    server = ClientProtocol.receive(packetIn);

	    if (server != null){
		    // Finally parse the response
		system_msg = ClientProtocol.parseResponse(packetIn, server);

		    // If system gives the signal, shut program down
		if (system_msg.equals("game over")){
		    server = null;
		}

	    }
	    else {
		System.out.println("error: specified server unreachable");
	    }

	} // end while true

	    // Close the socket
	try {
	    ClientProtocol.socket.close();
	}
	catch (Exception e){
	    e.printStackTrace();
	}

    } // end main method

    public static void addMailToQueue(String message){

	synchronized(mailbox){
	    mailbox.addLast(message);
	}

    } // end method addMailToQueue

    private static int checkMailbox(){

	int mailcount = 0;

	synchronized(mailbox){
	    mailcount = mailbox.size();
	}

	return mailcount;

    } // end method checkMailbox

    public static void createMailDaemon(String name, int port,
					IPAddress server){

	MailDaemon daemon = new MailDaemon(name, port, server);
	daemon.setDaemon(true);
	daemon.start();
	daemons.addLast(daemon);

    } // end method createMailDaemon

    public static void destroyMailDaemon(String name, IPAddress server){

	for (MailDaemon daemon : daemons){

	    if (daemon.matches(name,server)){
		daemon.endDaemon();
		break;
	    }

	} // end foreach daemon

    } // end method destroyMailDaemon


} // end class Client