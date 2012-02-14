import java.io.*;
import java.net.*;
import java.util.LinkedList;

/**
 * @desc This the server process that communicates
 *       with some client.
 */
public class Server
{

//    private ConfigFile config;
    private static DatagramSocket socket;
    private static int port;

	// Keep track of all the records known to the server
    private static LinkedList<Record> records = new LinkedList<Record>();


    public static void main(String[] args){

	    // Define a packet with which to receive data
	DatagramPacket packetIn = null;
	byte[] dataIn = new byte[Protocol.PACKET_SIZE];

	IPAddress client = null;

	boolean wait = false;

	port = 1648;


	    // Open the socket connection
	try {

	    socket = new DatagramSocket(port);
	    wait = true;

	    packetIn = new DatagramPacket(dataIn, dataIn.length);

	}
	catch (SocketException e) {
	    System.out.println("server could not open UDP port on port "
			       + port);
	}

	String system_msg = null;


	    // Loop forever until shutdown signal sent
	while (wait){

		// Create a new packet in which to receive data
	    dataIn = new byte[Protocol.PACKET_SIZE];
	    packetIn = new DatagramPacket(dataIn, dataIn.length);

		// Get message from the client
	    client = ServerProtocol.receive(socket, packetIn);

		// Parse the client response
	    system_msg = ServerProtocol.parseMessage(socket, packetIn, client);

		// If shutdown command sent, end looping
	    if (system_msg.matches("^.+ GAMEOVER$")){
		wait = false;
	    }

	} // end while wait


	    // Close the socket
	try {
	    socket.close();
	}
	catch (Exception e){ }

    } // end main

    /**
     * Add a new record to the list of records.
     */
    public static void addRecord(Record record){
	records.addLast(record);
    } // end method addRecord

    /**
     * Delete the specified record.
     *
     * @param name
     *    The name of the record to delete.
     * @param ipAddress
     *    The IP address of the record to delete. May be
     *    null.
     *
     * @return
     *    TRUE if the record could be deleted, FALSE otherwise.
     */
    public static boolean deleteRecord(String name, IPAddress ipAddress){

	boolean deleted = false;
	boolean match = false;

	Record deleteMe = null;
	String address = null;

	String addressTemp = null;

	    // Go through all the records
	for (Record temp : records){

	    match = false;

		// Try to match against the supplied name
	    match = temp.getName().equals(name);

		// If the supplied IP address isn't null, check
		// against it as well
	    if (ipAddress.getIPAddress() != null){
		address = ipAddress.getIPAddress();
		addressTemp = temp.getIPAddress().getIPAddress();
		match = match && addressTemp.matches(address);
	    }

		// Also check against supplied port number, if it
		// isn't == 0
	    if (ipAddress.getPort() > 0){
		match = match && (temp.getIPAddress().getPort()
					 == ipAddress.getPort());
	    }

		// If found, break from loop
	    if (match){
		deleteMe = temp;
		break;
	    }

	} // end foreach

	    // If a record was found, attempt to delete it
	if (deleteMe != null){
	    deleted = records.remove(deleteMe);
	}

	return deleted;

    } // end method deleteRecord

    /**
     * Find records that match the given regular expressions.
     *
     * @param name
     *    The regular expression against which to compare
     *    the name field of records.
     * @param address
     *    The regular expression against which to compare
     *    the IP address field of records.
     *
     * @return
     *    A LinkedList of Record objects that match the given
     *    regular expressions.
     */
    public static LinkedList<Record> findRecords(String name, String address){

	    // Parse the given regex for the address
	String addressRegex = IPAddress.parseIPAddressRegex(address);
	IPAddress ipRegex = new IPAddress(addressRegex);

	    // Create empty list in which to put matched records
	LinkedList<Record> matchedRecords = new LinkedList<Record>();

	    // Run checks against all records in the list
	for (Record temp : records){

	    if (temp.matches(name, ipRegex)){
		matchedRecords.addLast(temp);
	    } // end if temp.matches

	} // end foreach

	return matchedRecords;

    } // end method findRecords

} // end class Server