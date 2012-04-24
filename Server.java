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

    public static String name;
    private static int port = -1;
    public static IPAddress myIP;
    public static RoutingTable rtable;
    private static DatagramSocket socket;

	// Keep track of all the records known to the server
    private static LinkedList<Record> records = new LinkedList<Record>();

	// Keep track of all registered users
    public static LinkedList<RegisteredName> registrar =
		    new LinkedList<RegisteredName>();

	// Daemon thread that process network information
    public static AdminDaemon admin;
	// Daemon thread that checks link status
    public static LinkCheckDaemon linkChecker;

    public static void main(String[] args){

	recordFile = new RecordFile("Records.db");
	recordFile.addRecordsToList(records);

	    // Determine this host's IP and port
	determineIPAndPortNumber(args);

	    // Define a packet with which to receive data
	DatagramPacket packetIn = null;
	byte[] dataIn = new byte[Protocol.PACKET_SIZE];

	    // Start up the routing table
	rtable = new RoutingTable();

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

	    while (clientPort < Protocol.MIN_PORT ||
		   clientPort > Protocol.MAX_PORT){
		clientPort = r.nextInt(65535);
	    }

	    try {
		ClientProtocol.socket = new DatagramSocket(clientPort);
		break;
	    }
	    catch (SocketException e){ }

	} // end while true

	    // Start server admin daemon thread
	admin = new AdminDaemon();
	admin.setDaemon(true);
	admin.start();

	    // Start server link checker daemon thread
	linkChecker = new LinkCheckDaemon();
	linkChecker.setDaemon(true);
	linkChecker.start();


	String system_msg = null;

	    // Loop forever until shutdown signal sent
	while (wait){

		// Create a new packet in which to receive data
	    dataIn = new byte[Protocol.PACKET_SIZE_LARGE];
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

    /****************************************************


	utility methods


    *****************************************************/


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

	name = "";

	    // Select the name from the arguments first
	if (args.length == 2){
	    name = args[1];
	}
	    // If it isn't alphanumeric, read from config file
	if (!name.matches("[A-Za-z0-9!@#%&]+")){
	    name = config.getName();
	}
	    // Else name it "default"
	if (!name.matches("[A-Za-z0-9!@#%&]+")) {
	    name = "default";
	}

	InetAddress host = null;

	    // Print out IP address and port
	try {
	    host = InetAddress.getLocalHost();
	    System.out.println("IP is " + host.getHostAddress() +
			       ", port is " + port + ", name is " + name);
	    myIP = new IPAddress(host, port);
	}
	catch (UnknownHostException e){ }

    } // end determineIPAndPortNumber

    public static IPAddress getIPAddress(){
	return myIP;
    } // end getIPAddress


    /****************************************************


	methods to add/delete/find registered names


    *****************************************************/

    /**
     * Finds and returns the registered name, if it exists. 
     *
     * @param name
     *    The name on which to search.
     *
     * @return
     *    The RegisteredName object if found, or null if not found.
     */
    public static RegisteredName findRegisteredName(String name){

	RegisteredName rname = null;

	synchronized(registrar){
	    for (RegisteredName temp : registrar){
		if (temp.getName().equals(name)){
		    rname = temp;
		    break;
		} // end if name.matches()
	    } // end for RegisteredName
	} // end synchronized

	return rname;

    } // end findRegisteredName

    /**
     * Finds and returns the registered name, if it exists. 
     *
     * @param name
     *    The name on which to search.
     *
     * @return
     *    The RegisteredName object if found, or null if not found.
     */
    public static String getNameList(String names){

	String list = "";

	int i = 0;
	boolean match = false;
	int size = registrar.size();

	synchronized(registrar){
	    for (RegisteredName temp : registrar){

		if (names.indexOf(temp.getName()) > -1){
		    list += temp.getName();
		    match = true;
		}

		if (i < size-1 && match){
		    list += "\n";
		}

		i++;
		match = false;

	    } // end for RegisteredName
	} // end synchronized

	if (list.equals("")){
	    list = "no registered names on server";
	}

	return list;

    } // end findRegisteredName


    /**
     * Finds and returns the registered name given an IP, if it exists. 
     *
     * @param addr
     *    The IP addresss on which to search.
     *
     * @return
     *    The RegisteredName object if found, or null if not found.
     */
    public static RegisteredName findRegisteredNameIP(IPAddress addr){

	RegisteredName rname = null;
	IPAddress ip = null;

	synchronized(registrar){
	    for (RegisteredName temp : registrar){

		ip = temp.getIP(); 

		if (ip.getIPAddress().equals(addr.getIPAddress())){
		    rname = temp;
		    break;
		} // end if temp.getIPAddress

	    } // end for RegisteredName
	} // end synchronized

	return rname;

    } // end findRegisteredNameIP

    /**
     * Registers a name on the server.
     *
     * @param rname
     *    The RegisteredName to register.
     */
    public static void registerClient(RegisteredName rname){
	registrar.add(rname);
    }

    /**
     * Removes a registered name from the server.
     *
     * @param rname
     *    The RegisteredName to remove.
     */
    public static void removeClient(RegisteredName rname){
	synchronized(registrar){
	    registrar.remove(rname);
	} // end registrar
    }

    /**
     * Send mail to the recipients on this server.
     *
     * @param names
     *    A String array of names representing registered users.
     * @param message
     *    The message to send to the clients.
     */
    public static void sendMailToClients(String[] names, String message){

	    // Strip the message of ("\n.\n")
	message = message.replace("\n.\n","");

	    // If the user specified all registered names using the
	    // '*' character, send to all registered clients on the
	    // server
	if (names.length == 1 && names[0].equals("*")){
	    synchronized(registrar){
		for (RegisteredName name : registrar){
		    ClientProtocol.send(message, name.getMailAddress());
		}
	    }
	} // end if names.length

	    // Else only send mail to those names that are
	    // registered on this server
	else {

	    RegisteredName temp = null;

		// Go through each name and see if it exists
		// on this server
	    for (int i = 0; i < names.length; i++){

		    // Is name registered on this server?
		temp = findRegisteredName(names[i]);

		    // If so, send the mail
		if (temp != null){
		    ClientProtocol.send(message, temp.getMailAddress());
		}

	    } // end for i

	} // end else

    } // end sendMailToClients

    /**
     * Send mail to a given IP address.
     *
     * @param address
     *    An IPAddress to which the message is sent.
     * @param message
     *    The message to send to the IPAddress.
     */
    public static void sendMail(IPAddress address, String message){

	message = message.replace("\n.\n","");
	ClientProtocol.send(message, address);

    } // end sendMail    

} // end class Server