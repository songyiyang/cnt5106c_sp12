import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

/**
 * @desc Interprets and handles responses
 *       from the server and from user input.
 */
public class ClientProtocol extends Protocol
{

    private static String cmdRegex = "^(insert|delete|get|quit|" +
				     "gameover|help|\\?).*$";

    public static String parseCommand(String command){

	String system_msg = "WTF";
	ProtocolCommand cmd = null;

	if (command.matches(cmdRegex)){

	    IPAddress address = null;
	    String name = "";
	    String addr = "";
	    String portStr = "";
	    int port = 0;
	    String args = "";

	    String tokens[] = null;

	    BufferedReader in = null;
	    in = new BufferedReader(new InputStreamReader(System.in));

	    tokens = command.split("\\s");

		// Insert a new record
	    if (command.matches("^insert(\\s[A-Za-z0-9\\.]){0,3}.*")){

		name = parseParameter("Please enter the alphanumeric name " +
			 "(max 80 chars): ","[A-Za-z0-9]{1,80}", tokens, 1,
			 in, false);

		addr = parseParameter("Please enter the IP address:",
			 IPAddress.ipRegex, tokens, 2, in, false);

		while (port <= 1024 || port >= 65535){
		    portStr = parseParameter("Please enter the port number:",
			 "[0-9]{4,5}", tokens, 3, in, false);
		    port = Integer.parseInt(portStr);
		}

		address = new IPAddress(addr, port);

		cmd = ProtocolCommand.INSERT;

	    }

		// Delete a record from the server
	    else if (command.matches("^delete(\\s[A-Za-z0-9\\.]){0,2}.*")){

		name = parseParameter("Please enter the alphanumeric name " +
			 "(max 80 chars): ","[A-Za-z0-9]{1,80}", tokens, 1,
			 in, false);

		addr = parseParameter("Please enter the IP address:",
			 IPAddress.ipRegex, tokens, 2, in, true);

		if (addr.equals("!")){
		    addr = null;
		}

		while (port <= 1024 || port >= 65535){
		    portStr = parseParameter("Please enter the port number:",
			 "[0-9]{4,5}", tokens, 3, in, true);

		    if (portStr.equals("!")){
			port = 0;
			break;
		    }

		    port = Integer.parseInt(portStr);
		}

		address = new IPAddress(addr, port);

		cmd = ProtocolCommand.DELETE;

	    }

		// Delete a record from the server
	    else if (command.matches("^get(\\s[A-Za-z0-9\\.]){0,2}.*")){

		name = parseParameter("Please enter the alphanumeric name " +
			 "(max 80 chars): ","[A-Za-z0-9]{1,80}", tokens, 1,
			 in, false);

		addr = parseParameter("Please enter the IP address:",
			 IPAddress.ipRegex, tokens, 2, in, true);

		address = new IPAddress(addr, 0);

		cmd = ProtocolCommand.GET;

	    }


		// Force the client to exit
	    else if (command.equals("quit")){
		system_msg = "quit";
	    }

		// Shut down client and server
	    else if (command.equals("gameover")){
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

    public static String parseResponse(DatagramPacket packet,
				       IPAddress server){

	String system_msg = "";
	String message = Protocol.extractMessage(packet);
	String[] tokens = message.split("\\s+");

	ErrorCode error = null;

	System.out.println(message);

	if (tokens[1].equals("INSERT")){
	    system_msg = "INSERT";
	}

	else if (tokens[1].equals("DELETE")){
	    system_msg = "DELETE";
	}

	else if (tokens[1].equals("GET")){
	    system_msg = "GET";
	}

	else if (tokens[1].equals("GAMEOVER")){
	    system_msg = "game over";
	}

	else if (tokens[1].equals("TEST")){
	    system_msg = "test";
	}


	if (error != null){
	    System.out.println("error at server - " + error.getMessage());
	}

	return system_msg;

    } // end method parseResponse

    public static IPAddress receive(DatagramSocket socket,
				       DatagramPacket packet){
	return Protocol.receive(socket, packet);
    } // end method receive

    public static int send(DatagramSocket socket, String msg,
		              IPAddress address){
	return Protocol.send(socket, msg, address);
    } // end method send


    private static String parseParameter(String prompt, String regexp,
					 String[] tokens, int tokenIndex,
					 BufferedReader in, boolean optional){

	String value = "";
	String input = "";

	if (optional){
	    prompt = prompt.concat(("(type '!' to skip)"));
	}

	if (tokens.length >= tokenIndex+1 &&
	    tokens[tokenIndex].matches(regexp)){
	    value = tokens[tokenIndex];
	}

	while (!input.matches(regexp)){

	    System.out.print(prompt + " ");

	    try {
		 input = in.readLine();
	    }
	    catch (IOException e){ }

	    if (optional && input.equals("!")){
		break;
	    }

	} // end while !input.matches()

	if (value.equals("")){
	    value = input;
	}

	return value;

    } // end method parseParameter

} // end class ClientProtocol