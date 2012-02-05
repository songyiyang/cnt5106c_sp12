import java.net.DatagramSocket;
import java.net.DatagramPacket;

/**
 * @desc Interprets and handles responses
 *       from the server and from user input.
 */
public class ClientProtocol extends Protocol
{

    private static String cmdRegex = "^(quit|gameover|help|\\?).*$";

    public static String parseCommand(String input){

	String system_msg = "WTF";
	ProtocolCommand cmd = null;

	if (input.matches(cmdRegex)){

	    IPAddress address = null;
	    String name = null;
	   
	    if (input.equals("quit")){
		system_msg = "quit";
	    }
	    else if (input.equals("gameover")){
		cmd = ProtocolCommand.GAMEOVER;
	    }
	    else if (input.matches("^(help|\\?)$")){
//		system_msg = "help";
	    }


	    if (cmd != null){
		system_msg = ProtocolCommand.createPacket(cmd, name,
					        address, 0, false);
	    }

	} // end if input.matches

	return system_msg;

    } // end method parseCommand

    public static String parseResponse(DatagramPacket packet,
				       IPAddress server){

	String system_msg = "";
	String message = Protocol.extractMessage(packet);
	String[] tokens = message.split("\\s+");

	System.out.println(message);

	if (tokens[1].equals("GAMEOVER")){
	    system_msg = "game over";
	}
	else if (tokens[1].equals("TEST")){
	    system_msg = "test";
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

} // end class ClientProtocol