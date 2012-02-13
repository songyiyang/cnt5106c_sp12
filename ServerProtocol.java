import java.util.LinkedList;
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

	LinkedList<Record> matchedRecords = null;

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

	    matchedRecords = Server.findRecords(name, address);

	    args = args.concat("" + matchedRecords.size());

	    if (matchedRecords.size() == 0){
		error = ErrorCode.RECORD_NOT_FOUND;
	    }

	    cmd = ProtocolCommand.GET;

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

	if (cmd == ProtocolCommand.GET && error == null){
	    sendMatchedRecords(socket, client, matchedRecords);
	}

	return system_msg;

    } // end method parseMessage


    private static void sendMatchedRecords(DatagramSocket socket,
			  IPAddress client, LinkedList<Record> matchedRecords){

	String isEnd = " YAH";
	String isNotEnd = " NAW";
	int MESSAGE_PACKET_SIZE = 30;

	int packetSize = MESSAGE_PACKET_SIZE;
	int recordNum = 0;
	int numberOfRecords = matchedRecords.size();

	String name = null;
	String address = null;
	String message = "";
	String pmessage = "";

	String data = null;
	byte[] dataBytes = null;
	Record record = null;

	String response = null;
	DatagramPacket inPacket = null;
	ProtocolCommand cmd = ProtocolCommand.TRANSMIT;

	while (recordNum < numberOfRecords){

	    record = matchedRecords.get(recordNum);
	    name = record.getName();
	    address = record.getIPAddress().toString();

	    data = " " + name + " " + address;
	    dataBytes = data.getBytes();

	    if (packetSize + dataBytes.length < PACKET_SIZE){
		message = message + data;
		packetSize += dataBytes.length;
	    }
	    else {
		message = isNotEnd + message;
		pmessage = ProtocolCommand.createPacket(cmd, "", client,
						       message, 0, null);
		send(socket, pmessage, client);
		client = receive(socket, inPacket);

		response = Protocol.extractMessage(inPacket);

		if (response.matches(".*(SUCCESS)$")){
		    message = "";
		    packetSize = MESSAGE_PACKET_SIZE;
		}

		continue;

	    } // end else

	    recordNum++;

	} // end while recordNum

	if (packetSize > MESSAGE_PACKET_SIZE) {
	    message = isEnd + message;
	    pmessage = ProtocolCommand.createPacket(cmd, "", client,
						       message, 0, null);
	    send(socket, pmessage, client);
	    client = receive(socket, inPacket);

	    response = Protocol.extractMessage(inPacket);
	} // end if packetSize()

    } // end method 

    public static IPAddress receive(DatagramSocket socket,
				       DatagramPacket packet){
	return Protocol.receive(socket, packet);
    } // end method receive

    public static int send(DatagramSocket socket, String msg,
		              IPAddress address){
	return Protocol.send(socket, msg, address);
    } // end method send


} // end class ServerProtocol