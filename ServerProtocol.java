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
	ErrorCode error = null;

	String name = null;
	IPAddress ipAddress = null;
        int port = 0;
	String args = "";

	if (tokens[1].equals("INSERT")){

	    name = tokens[2];
	    String[] address = tokens[3].split(":");

	    if (address.length == 2){
		port = Integer.parseInt(address[1]);
		ipAddress = new IPAddress(address[0], port);
	    }
	    else {
		ipAddress = new IPAddress(address[0]);
	    }

	    Record record = new Record(name, ipAddress);
	    Server.addRecord(record);

	    cmd = ProtocolCommand.INSERT;

	}


	else if (tokens[1].equals("DELETE")){

	    name = tokens[2];
	    String[] address = tokens[3].split(":");
	    String ip = null;
	    port = 0;

	    if (!address[0].equals("null")){
		ip = address[0];
	    }
	    else {
		ip = null;
	    }

	    if (address.length == 2){
		port = Integer.parseInt(address[1]);
		ipAddress = new IPAddress(ip, port);
	    }
	    else {
		ipAddress = new IPAddress(ip);
	    }

	    boolean deleted = Server.deleteRecord(name, ipAddress);

	    if (!deleted){
		error = ErrorCode.RECORD_NOT_FOUND;
	    }

	    cmd = ProtocolCommand.DELETE;

	}



	else if (tokens[1].equals("GET")){

	    name = tokens[2];
	    String address = tokens[3];

	    cmd = ProtocolCommand.GET;
	    error = ErrorCode.RECORD_NOT_FOUND;

	}



	else if (tokens[1].equals("GAMEOVER")){
	    cmd = ProtocolCommand.GAMEOVER;
	}

	else if (tokens[1].equals("TEST")){
	    cmd = ProtocolCommand.TEST;
	}

	if (cmd != null){
	    system_msg = ProtocolCommand.createPacket(cmd, name,
					              client, args, 1, error);
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