import java.net.*;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Set;
import java.util.LinkedList;

/**
 * @desc This the thread that maintains network topology
 *       for a server. Each server will have one AdminDaemon
 *       running to test for neighboring links.
 */

public class AdminDaemon extends Thread
{

    int id;
    Transaction t;
    boolean shutdown;
    int historyLength;
    IPAddress daemonIP;
    DatagramSocket socket;
    DatagramPacket packet;

    private TreeMap<String,ClientList> clients;

    private LinkedList<Record> links;
    private LinkedList<Transaction> jobQueue;
    private LinkedList<String> processedMsgs;

    private static final int DEFAULT_MAX_HISTORY = 20;
    private static final long SLEEP_TIMER = 1000 * 30;

    public AdminDaemon(){

	id = 0;
	t = null;
	shutdown = false;
	historyLength = DEFAULT_MAX_HISTORY;

	links = new LinkedList<Record>();
	jobQueue = new LinkedList<Transaction>();
	processedMsgs = new LinkedList<String>();

	clients = new TreeMap<String,ClientList>();

	try {
	    socket = new DatagramSocket();
	    daemonIP = new IPAddress(Server.myIP.getIPAddress(),
		       socket.getPort());
	}
	catch (SocketException e){

	}

	resetPacket();

    } // end constructor

    public void run(){

	while (!shutdown){

	    if (!doJobsExist()){
		try {
		    sleep(SLEEP_TIMER);
		}
		catch (InterruptedException e){ }
	    }

	    processJob();

	} // end while !shutdown

    } // end method run

    public void addJobToQueue(Transaction transaction){

	synchronized(jobQueue){
	    jobQueue.addLast(transaction);
	}

    }

    private boolean doJobsExist(){

	boolean jobsExist = false;

	synchronized(jobQueue){
	    jobsExist = (jobQueue.size() > 0);
	}

	return jobsExist;

    }

    private void processJob(){

	getUnprocessedJob();

	if (t == null){
	    return;
	}

	String tranid = id + "-" + Server.getIPAddress().toString();

	    // Handle a LIST request
	if (t.getMessage().matches(".+LIST.+")){
	    // process LIST
	}

	    // Handle a SEND request
	else if (t.getMessage().matches(".+SEND.+")){
	    // process SEND
	}

	    // Handle registration requests
	else if (t.getMessage().matches(".+(UN)?REGISTER.+")){
	    processLinkCmd(tranid);	    
	}

	    // Handle registration requests
	else if (t.getMessage().matches(".+(UN)?LINK.+")){
	    processLinkCmd(tranid);
	}

	    // Handle some CONTROL message
	else {
	    // process CONTROL message
	    processControlCmd(tranid);
		// remember message in case someone sends this to you
	    addMessageToProcessedList();
	}

	id++;
	t = null;

    } // end processJob

    private void getUnprocessedJob(){

	Transaction temp;

	while (t == null && doJobsExist()){

	    temp = jobQueue.removeFirst();

	    if (!processedMsgs.contains(temp.getMessage())){
		t = temp;
	    }

	} // end while message

    } // end getUnprocessedJob


    private void processListCmd(){

	    // Convert names into string form

    }

    private void processSendCmd(){

	    // For each link, send out the message

    }


    private void processRegisterCmd(String tranid){

	String[] tokens = t.getMessage().split("\\s+");
	String message = "";
	String args = "";

	IPAddress rsp = null;


	if (t.getMessage().matches(".+REGISTER.+")){

	    message = ProtocolCommand.createPacket(ProtocolCommand.CTRL_ADD,
		      "", null, args, 0, null);

		// For each link, send message to register user for
		// this server
	    for (Record r : links){
//		ClientProtocol.send(message, r.getIPAddress());
//		rsp = ClientProtocol.receive(packet);
	    }

	}
	else {

	    // For each link, send message to unregister user
	    // for this server

	    message = ProtocolCommand.createPacket(ProtocolCommand.CTRL_RM,
		      "", null, args, 0, null);

		// For each link, send message to register user for
		// this server
	    for (Record r : links){
//		ClientProtocol.send(message, r.getIPAddress());
//		rsp = ClientProtocol.receive(packet);
	    }


	}

    } // end processRegisterCmd

    private void processLinkCmd(String tranid){

	String[] tokens = t.getMessage().split("\\s+");
	String message = "";
	String args = "";

	Record record = Server.getRecord(tokens[2]);
	IPAddress rsp = null;

	if (t.getMessage().matches(".+LINK.+")){

		// First, send a message to the remote server, listing off
		// our known contacts.
	    args = tranid + " " + daemonIP.toString() + " "
		   + generateClientList();

	    message = ProtocolCommand.createPacket(ProtocolCommand.CTRL_CONNECT,
		      "", null, args, 0, null);

	    send(message, record.getIPAddress());

		// Then, receive a message from the remote server and 
		// merge its client list into the one maintained here.
	    rsp = receive();

	    if (rsp != null){

		message = ClientProtocol.extract(packet);
	        tokens = message.split("\\s+");

		int numNames = Integer.parseInt(tokens[4]);

		if (numNames > 0){
		    mergeClientList(tokens[5]);
		}

	    }

	} // end if LINK

	    // Else it's an UNLINK command, remove data
	else {

	    args = tranid + " " + daemonIP.toString() + " "
		   + generateClientList();

		// First, disconnect from the remote server.
	    message = ProtocolCommand.createPacket(
			ProtocolCommand.CTRL_DISCONNECT, "", null, args, 0, null);
System.out.println("to: " + message);
	    send(message, record.getIPAddress());
	    rsp = receive();
	    message = ClientProtocol.extract(packet);
System.out.println("from: " + message);
		// Now, delete all the information the other server
		// passed on.
	    clients.remove(record.getIPAddress().toString());

		// Finally, ask remaining links for client information

	}

	resetPacket();

    } // end processLinkCmd


    private void processControlCmd(String tranid){

	String reply = "";
	String message = t.getMessage();
	String[] tokens = message.split("\\s+");
	String args = "";	

	IPAddress rsp;

	    // CTRL_CONNECT - send clients to user
	if (tokens[1].matches("CTRL_CONNECT")) {

	    args = tokens[2] + " " + daemonIP.toString() + " "
		   + generateClientList();

	    reply = ProtocolCommand.createPacket(ProtocolCommand.CTRL_CONNECT,
		         "", null, args, 1, null);

	    send(reply, t.getIP());

	    int numNames = Integer.parseInt(tokens[4]);

	    if (numNames > 0){
		mergeClientList(tokens[5]);
	    }

	}

	    // CTRL_DISCONNECT - delete clients used by user
	else if (tokens[1].matches("CTRL_DISCONNECT")) {

	    args = tranid + " " + daemonIP.toString();

	    reply = ProtocolCommand.createPacket(ProtocolCommand.CTRL_CONNECT,
			  "", null, args, 1, null);

	    send(message, t.getIP());

	    clients.remove(t.getIP().toString());

	}

	// CTRL_ADD

	// CTRL_RM

	// CTRL_SEND

	// CTRL_UPDATE

	resetPacket();

    }


    private String generateClientList(){

	String clientData = "";
	int numSemicolons = 0;
	int i = 0;
	int count = 0;

	synchronized(Server.registrar){

	    numSemicolons = Server.registrar.size() - 1;
	    i = 0;

	    for (RegisteredName rn : Server.registrar){

		count++;

		clientData += Server.getIPAddress().toString() + "#" +
			      rn.getName();

		if (i < numSemicolons){
		    clientData += ";";
		    i++;
		}

	    } // end foreach rn

	} // end synchronized

	    // Get all the keys in the mapping
	Set<String> set = clients.keySet();

	    // For each key, add server names
	for (String key : set){

	    ClientList list = clients.get((Object)key);

	    numSemicolons = list.size() - 1;
	    i = 0;

	    for (String name : list.getList()){

		count++;

		clientData += key + "#" + name;

		if (i < numSemicolons){
		    clientData += ";";
		    i++;
		}

	    } // end foreach key

	}

	clientData = "" + count + " " + clientData;

	return clientData;

    } // end generateClientList

    private void mergeClientList(String toParse){

	String[] pairs = toParse.split(";");

	for (int i = 0; i < pairs.length; i++){

	    String[] pair = pairs[i].split("#");

		// Add client if key exists
	    if (!clients.containsKey(pair[0])){
		clients.put(pair[0], new ClientList());
	    }

	    clients.get(pair[0]).add(pair[1]);

	    System.out.println(pair[0] + " " + pair[1] );

	} // end for int i

    } // end mergeClientList

    private void addMessageToProcessedList(){

	if (processedList.size() == historyLength){

	    int numToRemove = historyLength / 2;

	    for (int i = 0; i < numToRemove; i++){
		processedList.removeFirst();
	    }
	}

        processedList.addLast(t.getMessage());

    } // end addMessageToProcessedList


    private IPAddress receive(){

	IPAddress addr = null;

	try {
	    addr = Protocol.receive(socket, packet);
	}
	catch (SocketTimeoutException e){ }

	return addr;

    }

    private void send(String message, IPAddress recipient){
	Protocol.send(socket, message, recipient);
    }

    public void endDaemon(){
	shutdown = true;
    } // end endDaemon

    private void resetPacket(){
	packet = new DatagramPacket(new byte[Protocol.PACKET_SIZE_LARGE],
				    Protocol.PACKET_SIZE_LARGE);
    } // end resetPacket

} // end class MailDaemon