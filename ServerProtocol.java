import java.util.LinkedList;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;

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

	    // Insert a record
	if (tokens[1].equals("INSERT")){

	    name = tokens[2];
	    String[] address = tokens[3].split(":");

		// Build the IPAddress based on passed parameter
		// values
	    if (address.length == 2){
		port = Integer.parseInt(address[1]);
		ipAddress = new IPAddress(address[0], port);
	    }
	    else {
		ipAddress = new IPAddress(address[0]);
	    }

		// Create the record and add it to the list
	    Record record = new Record(name, ipAddress);
	    Server.addRecord(record);

	    cmd = ProtocolCommand.INSERT;

	}

	    // Delete a record
	else if (tokens[1].equals("DELETE")){

	    name = tokens[2];
	    String[] address = tokens[3].split(":");
	    String ip = null;
	    port = 0;

		// Set the String IP to a non-null value if it exists.
		// Otherwise set to null
	    if (!address[0].equals("null")){
		ip = address[0];
	    }
	    else {
		ip = null;
	    }

		// If port was passed, add to IPAddress. Otherwise
		// just add the String IP.
	    if (address.length == 2){
		port = Integer.parseInt(address[1]);
		ipAddress = new IPAddress(ip, port);
	    }
	    else {
		ipAddress = new IPAddress(ip);
	    }

		// Attempt to delete the record
	    boolean deleted = Server.deleteRecord(name, ipAddress);

		// If record could be deleted for some reason,
		// set the error code to RECORD_NOT_FOUND
	    if (!deleted){
		error = ErrorCode.RECORD_NOT_FOUND;
	    }

	    cmd = ProtocolCommand.DELETE;

	}


	    // Check to see if there are records to return
	else if (tokens[1].equals("GET")){

	    name = tokens[2];
	    String address = tokens[3];

		// Attempt to find records
	    matchedRecords = Server.findRecords(name, address);

		// If no records have been found, set error code
		// to RECORD_NOT_FOUND
	    if (matchedRecords.size() == 0){
		error = ErrorCode.RECORD_NOT_FOUND;
	    }

	    cmd = ProtocolCommand.GET;

	}


	    // Process a shutdown request
	else if (tokens[1].equals("GAMEOVER")){

	    // For future project: detect if server is
	    // busy and return the SERVER_BUSY error code
	    // if it is. Right now it is a single-threaded
	    // system so it will not be doing additional
	    // processing in a thread

	    cmd = ProtocolCommand.GAMEOVER;

	}

	    // Client sent test message
	else if (tokens[1].equals("TEST")){
	    cmd = ProtocolCommand.TEST;
	}

	    // If the server needs to respond, send response
	    // to the client
	if (cmd != null){
	    system_msg = ProtocolCommand.createPacket(cmd, name,
					              client, args, 1, error);
	    send(socket, system_msg, client);
	}

	    // In the event that the server has found records for
	    // a GET request, begin sending the records to the client
	if (cmd == ProtocolCommand.GET && error == null){
	    sendMatchedRecords(socket, client, matchedRecords);
	}

	return system_msg;

    } // end method parseMessage


    /**
     * Send records to the client. This method is called
     * when the client sends a GET request and the server
     * can match records to the regex sent from the client.
     *
     * @param socket
     *    The socket through which to communicate with the
     *    client
     * @param client
     *    The IPAddress object representing the client.
     * @param matchedRecords
     *    LinkedList of records that match the user's
     *    regex
     *
     */
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

	    inPacket = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

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
	IPAddress addr = null;

	try {
	    addr = Protocol.receive(socket, packet);
	}
	catch (SocketTimeoutException e){ }

	return addr;

    } // end method receive

    public static boolean send(DatagramSocket socket, String msg,
		               IPAddress address){
	return Protocol.send(socket, msg, address);
    } // end method send


} // end class ServerProtocol