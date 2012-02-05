import java.net.DatagramSocket;
import java.net.DatagramPacket;

/**
 * @desc Interprets and handles messages
 *       from the client.
 */
public class ServerProtocol extends Protocol
{

    public static String parseMessage(DatagramSocket socket,
			 DatagramPacket packet, IPAddress client){

	String system_msg = "";
	String message = Protocol.extractMessage(packet);
	String[] tokens = message.split("\\s+");

	ProtocolCommand cmd = null;
	boolean error = false;

	String name = null;
        int port = 0;

	if (tokens[1].equals("GAMEOVER")){
	    cmd = ProtocolCommand.GAMEOVER;
	}
	else if (tokens[1].equals("TEST")){
	    cmd = ProtocolCommand.TEST;
	}

	if (cmd != null){
	    system_msg = ProtocolCommand.createPacket(cmd, name,
					              client, 1, error);
	    send(socket, system_msg, client);
	}


	return system_msg;

    } // end method parseMessage


    public static IPAddress receive(DatagramSocket socket,
				       DatagramPacket packet){
	return Protocol.receive(socket, packet);
    } // end method receive

    public static int send(DatagramSocket socket, String msg,
		              IPAddress address){
	return Protocol.send(socket, msg, address);
    } // end method send


} // end class ServerProtocol