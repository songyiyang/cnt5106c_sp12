import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

/**
 * @desc Interprets and handles responses
 *       from the server and from user input.
 */
public class ClientProtocol extends Protocol
{

	// Define the basic inputs
    private static String cmdRegex = "^(server|insert|delete|find|quit|" +
				     "kill|help|\\?).*$";

    private static boolean timeout = false;

    /**
     * Parse the response from the server. May invoke threads
     * or other send/receive pairs in order to complete the
     * the transaction.
     *
     * @param command
     *    The command sent from the user.
     * @param packet
     *    The packet that contains the server's response.
     * @param server
     *    The IPAddress of the server.
     *
     * @return
     *    A string representing a system message.
     *    The message may represent something that should
     *    be sent to the server, or that should be handled
     *    solely by the client.
     */
    public static String parseCommand(String command, DatagramSocket socket){

	    // Set default messages and cmd variables
	String system_msg = "WTF";
	ProtocolCommand cmd = null;

	    // Reset the timeout flag
	timeout = false;

	if (command.matches(cmdRegex)){

	    IPAddress address = null;
	    String name = "";
	    String addr = "";
	    String portStr = "";
	    int port = 0;
	    String args = "";

	    String tokens[] = null;

		// Need user input for most commands
	    BufferedReader in = null;
	    in = new BufferedReader(new InputStreamReader(System.in));

		// Split the command into separate chunks
	    tokens = command.split("\\s+");

		// Insert a new record
	    if (command.matches("^insert(\\s[A-Za-z0-9\\.]){0,3}.*")){

		    // The name to insert at remote site
		name = parseParameter("Please enter the alphanumeric name " +
			 "(max 80 chars): ","[A-Za-z0-9]{1,80}", tokens, 1,
			 in, false);

		    // The IP address of the remote site
		addr = parseParameter("Please enter the IP address:",
			 IPAddress.ipRegex, tokens, 2, in, false);
		    // The port number of the remote socket
		    // Note the port can be anything that isn't 0
		while (port < 1024 || port > 65535){
		    portStr = parseParameter("Please enter the port number:",
			 "^[1-9][0-9]{3,4}$", tokens, 3, in, false);
		    port = Integer.parseInt(portStr);
		}

		address = new IPAddress(addr, port);

		cmd = ProtocolCommand.INSERT;

	    }

		// Delete a record from the server
	    else if (command.matches("^delete(\\s[A-Za-z0-9\\.]){0,2}.*")){

		    // Get the name of the record to delete
		name = parseParameter("Please enter the alphanumeric name " +
			 "(max 80 chars): ","[A-Za-z0-9]{1,80}", tokens, 1,
			 in, false);

		    // Get the IP address to delete
		    // This field is optional
		addr = parseParameter("Please enter the IP address:",
			 IPAddress.ipRegex, tokens, 2, in, true);

		    // If ignore sentinel was passed, null out variable
		if (addr.equals("!")){
		    addr = null;
		}

		    // Get the port of the record to delete
		    // This field is optional
		while (port < 1024 || port > 65535){
		    portStr = parseParameter("Please enter the port number:",
			 "^[1-9][0-9]{3,4}$", tokens, 3, in, true);

		    if (portStr.equals("!")){
			port = 0;
			break;
		    }

		    port = Integer.parseInt(portStr);
		}

		    // Create IPAddress object. addr and port may be
		    // set to ignore values, so it might not
		    // represent a real IP/port pair
		address = new IPAddress(addr, port);

		cmd = ProtocolCommand.DELETE;

	    }

		// Get records from the server
	    else if (command.matches("^find(\\s[A-Za-z0-9\\.]){0,2}.*")){

		    // Get the regex for the name
		name = parseParameter("Please enter the alphanumeric name " +
			 "(max 80 chars): ","([A-Za-z0-9]{1,80}|\\*{1})",
			 tokens, 1, in, false);

		    // Get the regex for the IP address
		addr = parseParameter("Please enter the IP address:",
			 IPAddress.ipRegexWildcard, tokens, 2, in, false);

		    // Only the name and IP regex will be checked
		    // against records, port is not important
		address = new IPAddress(addr, 0);

		cmd = ProtocolCommand.GET;

	    }

		// Set the address and port of the server
	    else if (command.matches("^server(\\s[A-Za-z0-9\\.]){0,2}.*")){

		IPAddress tempAddr = null;

		    // Get the regex for the IP address
		addr = parseParameter("Please enter the IP address:",
			 IPAddress.ipRegex, tokens, 2, in, false);

		    // The port number of the remote socket
		    // Note the port can be anything that isn't 0
		while (port < 1024 || port > 65535){
		    portStr = parseParameter("Please enter the port number:",
			 "^[1-9][0-9]{3,4}$", tokens, 3, in, false);
		    port = Integer.parseInt(portStr);
		}

		    // Attempt to ping the remote server
		try {

		    int TIMEOUT = 1000 * 5;
		    InetAddress tempInet = InetAddress.getByName(addr);
		    tempAddr = new IPAddress(tempInet, port);

		    String tempMsg = ProtocolCommand.createPacket(
				   ProtocolCommand.TEST, "", null, "", 0, null);
		    DatagramPacket packet = new DatagramPacket(
					    new byte[PACKET_SIZE], PACKET_SIZE);

		    socket.setSoTimeout(TIMEOUT);

		    send(socket, tempMsg, tempAddr);
		    tempAddr = receive(socket, packet);

		}

		catch (Exception e){ }

		    // Reset the timeout counter
		try {
		    socket.setSoTimeout(0);
		}
		catch (SocketException e) { }


		if (tempAddr == null){
		    System.out.println("error: specified server unreachable");
		    system_msg = "set";
		}
		else {
		    System.out.println("Server IP = " + addr + ", " +
				       "port = " + port);
		    system_msg = "set " + tempAddr.toString();
		}


	    }

		// Force the client to exit
	    else if (command.equals("quit")){
		system_msg = "quit";
	    }

		// Shut down server
	    else if (command.equals("kill")){
		cmd = ProtocolCommand.GAMEOVER;
	    }

		// Get system help
	    else if (command.matches("^(help|\\?)$")){
//		system_msg = "help";
	    }


	    if (cmd != null){
		system_msg = ProtocolCommand.createPacket(cmd, name,
					        address, args, 0, null);
	    }

	} // end if input.matches

	return system_msg;

    } // end method parseCommand

    /**
     * Parse the response from the server. May invoke threads
     * or other send/receive pairs in order to complete the
     * the transaction.
     *
     * @param socket
     *    The DatagramSocket through which to talk to the
     *    server.
     * @param packet
     *    The packet that contains the server's response.
     * @param server
     *    The IPAddress of the server.
     *
     * @return
     *    A string representing some system message.
     *    The client may act on the system or may
     *    ignore it, depending on the type of message.
     */
    public static String parseResponse(DatagramSocket socket,
				       DatagramPacket packet,
				       IPAddress server){

	String system_msg = "";
	String message = Protocol.extractMessage(packet);
	String[] tokens = message.split("\\s+");

	ErrorCode error = null;

	if (tokens.length >= 3 && tokens[2].equals("ERROR")){
	    int errorNumber = Integer.parseInt(tokens[3]);
	    error = ErrorCode.getErrorCode(errorNumber);
	}

	if (tokens[1].equals("INSERT")){
	    system_msg = "INSERT";
	}

	else if (tokens[1].equals("DELETE")){
	    system_msg = "DELETE";
	}

	else if (tokens[1].equals("GET")){

	    if (error == null){
		getMatchedRecords(socket, server);
	    }

	    system_msg = "GET";

	}

	else if (tokens[1].equals("GAMEOVER")){
	    system_msg = "game over";
	}

	else if (tokens[1].equals("TEST")){
	    system_msg = "test";
	}


	if (error != null){
	    System.out.println("error: " + error.getMessage());
	}

	return system_msg;

    } // end method parseResponse

    /**
     * Get records from the server. After a "get" command,
     * if the server does not indicate an error, the server
     * sends the matched records to the client. This method
     * gets the records and prints them to the screen one
     * by one.
     *
     * @param socket
     *    The DatagramSocket through which to talk to the
     *    server.
     * @param server
     *    The IPAddress of the server.
     */
    private static void getMatchedRecords(DatagramSocket socket,
					  IPAddress server){

	boolean getRecords = true;
	DatagramPacket inPacket = null;
	ProtocolCommand cmd = ProtocolCommand.TRANSMIT;

	String name = null;
	String address[] = null;

	String message = null;
	String response = null;
	String tokens[] = null;

	int i = 0;

	    // While there are records to get, print them out
	while (getRecords){

	    inPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

		// Ask for data
	    server = receive(socket, inPacket);

		// Parse packet for records
	    response = Protocol.extractMessage(inPacket);
	    tokens = response.split("\\s+");

		// If this is the last packet, stop looping
	    if (tokens[2].equals("YAH")){
		getRecords = false;
	    }

		// Loop through packet message and get data
	    i = 3;
	    while (i < tokens.length){

		name = tokens[i];
		address = tokens[i+1].split("\\:");

		    // Print out record information
		System.out.println("Record: name = " + name +
				   ", IP = " + address[0] +
				   ", port = " + address[1]);

		i += 2;

	    } // end while i

		// Inform server of success/failure
	    message = ProtocolCommand.createPacket(cmd, "", null, "", 1, null);
	    send(socket, message, server);

	} // end while getRecords

    } // end method getMatchedRecords

    /**
     * Request the user to enter properly-formatted data, for
     * use when filling out a command
     *
     * @param prompt
     *    The text that should display to the user.
     * @param regexp
     *    The regular expression against which to check the
     *    user input.
     * @param tokens
     *    An array representing the command passed by the user.
     *    Used to decide if the user passed in a value for the
     *    command parameter.
     * @param tokenIndex
     *    The index of tokens that contains the passed in
     *    parameter, if it exists.
     * @param in
     *    The input stream that will capture user input.
     * @param optional
     *    Flag used to determine if the parameter can be ignored
     *    by the user.
     *
     * @return
     *    A String representing the value passed in by 
     *    the user.
     */
    private static String parseParameter(String prompt, String regexp,
					 String[] tokens, int tokenIndex,
					 BufferedReader in, boolean optional){

	String value = "";
	String input = "";

	    // If this parameter is optional, tell the user how
            // to skip entering this parameter
	if (optional){
	    prompt = prompt.concat(("(type '!' to skip)"));
	}

	    // Check to see if the user already passed
	    // the parameter
	if (tokens.length >= tokenIndex+1 &&
	    tokens[tokenIndex].matches(regexp)){
	    value = tokens[tokenIndex];
	}

	    // Keep looping until user passes in an acceptable value
	while (!input.matches(regexp) && value.equals("")){

		// Print out the prompt for input
	    System.out.print(prompt + " ");

		// Read in the parameter
	    try {
		 input = in.readLine();
	    }
	    catch (IOException e){ }

		// If the parameter is optional, and user
		// wants to ignore parameter, then break
		// from loop
	    if (optional && input.equals("!")){
		break;
	    }

	} // end while !input.matches()

	    // If something wasn't initially passed,
	    // get the input that the user passed
	if (value.equals("")){
	    value = input;
	}

	return value;

    } // end method parseParameter

    /**
     * Receive a packet from some server.
     *
     * @param socket
     *    The DatagramSocket through which to talk to the
     *    server.
     * @param packet
     *    The packet through which the data is received.
     *
     * @return
     *    The IPAddress of the server from which the packet
     *    originated.
     */
    public static IPAddress receive(DatagramSocket socket,
				       DatagramPacket packet){
	IPAddress addr = null;

	try {
	    addr = Protocol.receive(socket, packet);
	}
	catch (SocketTimeoutException e){
	    // do something here
	}

	return addr;

    } // end method receive

    /**
     * Send a packet to some server.
     *
     * @param socket
     *    The DatagramSocket through which to talk to the
     *    server.
     * @param msg
     *    The message to send.
     * @param packet
     *    The packet through which the data is received.
     *
     * @return
     *    TRUE if the send operation was successful, FALSE
     *    otherwise.
     */
    public static boolean send(DatagramSocket socket, String msg,
		              IPAddress address){
	return Protocol.send(socket, msg, address);
    } // end method send

} // end class ClientProtocol