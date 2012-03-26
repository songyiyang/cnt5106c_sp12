import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.LinkedList;

/**
 * @desc This the server process that communicates
 *       with some client.
 */
public class Server
{

    private static RecordFile recordFile;
    private static ConfigFile config;
    private static DatagramSocket socket;
    private static int port;

	// Keep track of all the records known to the server
    private static LinkedList<Record> records = new LinkedList<Record>();


    public static void main(String[] args){

	recordFile = new RecordFile("Records.db");
	recordFile.addRecordsToList(records);

	    // Determine this host's IP and port
	determineIPAndPortNumber(args);

	    // Define a packet with which to receive data
	DatagramPacket packetIn = null;
	byte[] dataIn = new byte[Protocol.PACKET_SIZE];

	IPAddress client = null;

	boolean wait = false;


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


	    // Try to open a socket on the server for client connections

	DatagramSocket clientSocket;
	Random r = new Random();
	int clientPort = 0;

	    // Create a socket that the server may use as a temporary
	    // client to some other server.
	while (true) {

	    while (clientPort < 1024 || clientPort > 65535){
		clientPort = r.nextInt(65535);
	    }

	    try {
		ClientProtocol.socket = new DatagramSocket(clientPort);
		break;
	    }
	    catch (SocketException e){ }

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
	    if (system_msg.matches("gameover")){
		wait = false;
	    }

	} // end while wait


	recordFile.writeRecordsToFile(records);

	    // Close the socket
	try {
	    socket.close();
	}
	catch (Exception e){ }

    } // end main

    /**
     * Add a new record to the list of records.
     */
    public static boolean addRecord(Record record){

	boolean added = false;
	boolean duplicateExists = false;

	for (Record temp : records){
	    if (temp.getName().equals(record.getName())){
		duplicateExists = true;
		break;
	    }
	}

	if (!duplicateExists){
	    records.addLast(record);
	    added = true;
	}

	return added;

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
     * Prints out the IP and port number of the server.
     * This method also determines what the port number 
     * will be.
     * 
     * @param args[]
     *    An array of Strings as passed to main()
     * 
     */
    private static void determineIPAndPortNumber(String args[]){

	config = new ConfigFile("config.ini");
	boolean finished = false;

	int selectedPort = 0;

	    // Highest priority is given to command-line argument
	if (args.length >= 1){
		selectedPort = Integer.parseInt(args[0]);
	    try {
		if (selectedPort >= 1024 && selectedPort <= 65535){
		    finished = true;
		}
	    }
	    catch (NumberFormatException e){ 

	    }
	} // end if args.length

	    // If no argument is passed, or passed arg was not a valid
	    // port number, read in the configuration file
	if (!finished){
		// Second-highest priority is given to server config file
	    selectedPort = config.getPort();

	    if (selectedPort >= 1024 && selectedPort <= 65535){
		finished = true;
	    }
	}


	    // If someone modified the config file to have an invalid
	    // port, then randomly choose one. it's getting reported to the
	    // user anyhow, until it gets changed of course.
	if (!finished){
	    selectedPort = 8080;
	    finished = true;
	}

	    
	port = selectedPort;

	InetAddress host = null;

	    // Print out IP address and port
	try {
	    host = InetAddress.getLocalHost();
	    System.out.println("IP is " + host.getHostAddress() +
			       ", port is " + port);
	}
	catch (UnknownHostException e){ }

    } // end determineIPAndPortNumber

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

    public static int getPort(){
	return port;
    }

} // end class Server