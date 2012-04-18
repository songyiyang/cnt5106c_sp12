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
	    daemonIP = Server.myIP;
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
	    processListCmd();
	}

	    // Handle a SEND request
	else if (t.getMessage().indexOf("SEND") > 0){
	    processSendCmd(tranid);
	}

	    // Handle registration requests
	else if (t.getMessage().matches(".+(UN)?REGISTER.+")){
	    processRegisterCmd(tranid);	    
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

	String[] tokens = t.getMessage().split("\\s+");
	String names = tokens[2];
	String servers = tokens[3];

	String data = "";
	String args = "";

	String nameRegexp = "";
	String serverRegexp = "";

	if (!names.equals("*")){
	    nameRegexp = "(" + names.replaceAll(",", "|") + ")";
	}

	if (!servers.equals("*")){
	    serverRegexp = servers.replaceAll("SELF","");
	    serverRegexp = servers.replaceAll(",,", ",");
	    serverRegexp = "(" + servers.replaceAll(",", "|") + ")";
	}


	if (servers.indexOf("SELF") >= 0 || servers.equals("*")){

	    for (RegisteredName rn : Server.registrar){

		if (!names.equals("*") && !rn.getName().matches(nameRegexp)){
		    continue;
		}

		data += "SELF:" + rn.getName() + ";";
	    }

	}


	    // For each link, send out the message

	if (clients.size() > 0){

	    Set<String> keys = clients.keySet();
	    LinkedList<String> nameList = null;

	    for (String key : keys){

		if (!servers.equals("*") && !key.matches(serverRegexp)){
		    continue;
		}

		nameList = clients.get(key).getList();

		for (String entry : nameList){

		    if (!names.equals("*") && !entry.matches(nameRegexp)){
			continue;
		    }

		    data += key + ":" + entry + ";";
		}

	    } // end foreach keys
	}

	if (data.equals("")){
	    data = "-";
	}

	args = tokens[2] + " " + tokens[3] + " " + data;

	String message = ProtocolCommand.createPacket(ProtocolCommand.LIST,
			      "", null, args, 1, null);

	send(message, t.getIP());

    }

    private void processSendCmd(String tranid){

	String message = t.getMessage();
	String[] tokens = t.getMessage().split("\\s+");
	String names = tokens[2];
	String servers = tokens[3];
	String args = "";

	int msgPos = message.indexOf("! ");
	String toSend = message.substring(msgPos+2);
	IPAddress rsp;

	if (servers.indexOf("SELF") >= 0 || servers.equals("*")){
	    Server.sendMailToClients(names.split(","), toSend);
	    servers = servers.replaceAll("SELF","");
	    servers = servers.replaceAll(",,", ",");
	}

	if (!names.equals("*")){
	    names = "(" + names.replaceAll(",", "|") + ")";
	}

	if (!servers.equals("*")){
	    servers = "(" + servers.replaceAll(",", "|") + ")";
	}

	args = tranid + " " + tokens[2];

	if (servers.equals("*")){
	    args += " yes";
	}
	else {
	    args += " no";
	}

	    // For each link, send out the message
	for (Record r : links){

	    if (!r.getName().matches(servers) && !servers.equals("*")){
		continue;
	    }

		// Send message with information

	    message = ProtocolCommand.createPacket(ProtocolCommand.CTRL_SEND,
		      "", null, args, 0, null);

	    send(message, r.getIPAddress());
	    rsp = receive();

	} // end foreach keys

	addMessageToProcessedList(message);

    }


    private void processRegisterCmd(String tranid){

	String[] tokens = t.getMessage().split("\\s+");
	String message = "";
	String args = "";

	IPAddress rsp = null;


	if (t.getMessage().matches(".+REGISTER.+")){

		// First, create the message to the remote servers
	    args = tranid + " " + tokens[2];

	    message = ProtocolCommand.createPacket(ProtocolCommand.CTRL_ADD,
		      "", null, args, 0, null);

		// For each link, send message to register user for
		// this server
	    for (Record r : links){
		send(message, r.getIPAddress());
		rsp = receive();
		resetPacket();
	    }

	}
	else {

		// First, create the message to the remote servers
	    args = tranid + " " + tokens[2];

	    message = ProtocolCommand.createPacket(ProtocolCommand.CTRL_RM,
		      "", null, args, 0, null);

		// For each link, send message to register user for
		// this server
	    for (Record r : links){
		send(message, r.getIPAddress());
		rsp = receive();
		resetPacket();
	    }

	}

	addMessageToProcessedList(message);

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
	    args = tranid + " " + generateClientList();

	    message = ProtocolCommand.createPacket(ProtocolCommand.CTRL_CONNECT,
		      "", null, args, 0, null);

	    send(message, record.getIPAddress());

		// Then, receive a message from the remote server and 
		// merge its client list into the one maintained here.
	    rsp = receive();

	    if (rsp != null){

		links.addLast(record);

		message = ClientProtocol.extract(packet);
	        tokens = message.split("\\s+");

		int numNames = Integer.parseInt(tokens[3]);

		if (numNames > 0){
		    mergeClientList(tokens[4]);
		}

	    }

	} // end if LINK

	    // Else it's an UNLINK command, remove data
	else {

	    args = tranid;

		// First, disconnect from the remote server.
	    message = ProtocolCommand.createPacket(
			ProtocolCommand.CTRL_DISCONNECT, "",
			null, args, 0, null);

	    send(message, record.getIPAddress());
	    rsp = receive();
	    message = ClientProtocol.extract(packet);

		// Now, delete all the information the other server
		// passed on.
	    clients.remove(record.getIPAddress().toString());
	    links.remove(record);

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

	    args = tokens[2] + " " + generateClientList();

	    reply = ProtocolCommand.createPacket(ProtocolCommand.CTRL_CONNECT,
		         "", null, args, 1, null);

	    send(reply, t.getIP());

	    String idParts[] = tokens[2].split("-");

	    links.addLast(new Record(idParts[1], t.getIP(), true));

	    int numNames = Integer.parseInt(tokens[3]);

	    if (numNames > 0){
		mergeClientList(tokens[4]);
	    }

	}

	    // CTRL_DISCONNECT - delete clients used by user
	else if (tokens[1].matches("CTRL_DISCONNECT")) {

	    args = tokens[2];

	    reply = ProtocolCommand.createPacket(
			    ProtocolCommand.CTRL_DISCONNECT, "", null,
			    args, 1, null);

	    send(message, t.getIP());

	    String idParts[] = tokens[2].split("-");

	    int index = 0;
	    String name = "";
	    if (links.size() > 0){
		for (Record temp : links){
		    if (temp.getName().equals(idParts[1])){
			name = temp.getName();
			break;
		    }
		}
		links.remove(index);
	    }

	    clients.remove(name);

	}

	    // CTRL_ADD - add a registered name to list
	else if (tokens[1].matches("CTRL_ADD")) {

	    String[] idParts = tokens[2].split("-");

	    args = tokens[2] + " " + tokens[3];

	    reply = ProtocolCommand.createPacket(ProtocolCommand.CTRL_ADD,
			  "", null, args, 1, null);

	    send(message, t.getIP());

	    if (!clients.containsKey(idParts[1])){
		clients.put(tokens[1],new ClientList());
	    }


	    clients.get(idParts[1]).add(tokens[3]);


	    for (Record temp : links){
		if (!temp.getName().equals(idParts[1])){
		    send(message, temp.getIPAddress());
		    rsp = receive();
		    resetPacket();
		} // end if
	    } // end foreach

	}

	    // CTRL_RM - remove a registered name from the list
	else if (tokens[1].matches("CTRL_RM")) {

	    String[] idParts = tokens[2].split("-");

	    args = tokens[2] + " " + tokens[3];

	    reply = ProtocolCommand.createPacket(ProtocolCommand.CTRL_RM,
			  "", null, args, 1, null);

	    send(message, t.getIP());

	    if (clients.containsKey(idParts[1])){
		clients.get(idParts[1]).remove(tokens[3]);
	    }

	    for (Record temp : links){
		send(message, temp.getIPAddress());
		rsp = receive();
		resetPacket();
	    }

	}

	    // CTRL_SEND - remove a registered name from the list
	else if (tokens[1].matches("CTRL_SEND")) {

	    int msgPos = message.indexOf("! ");
	    String toSend = message.substring(msgPos+2);

	    args = tokens[2] + " " + tokens[3] + " " + tokens[4];

	    reply = ProtocolCommand.createPacket(ProtocolCommand.CTRL_SEND,
			  "", null, args, 1, null);

	    send(message, t.getIP());

		// Send mail to users here
	    Server.sendMailToClients(tokens[3].split(","),toSend);

		// Send mail to others, if required
	    if (tokens[5].equals("yes")){
		for (Record temp : links){
		    send(message, temp.getIPAddress());
		    rsp = receive();
		    resetPacket();
		} // end foreach
	    } // end if tokens

	}

	    // CTRL_UPDATE

	resetPacket();

    } // end processControlCmd


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

//	    System.out.println(pair[0] + " " + pair[1] );

	} // end for int i

    } // end mergeClientList

    private void addMessageToProcessedList(){

	if (processedMsgs.size() == historyLength){

	    int numToRemove = historyLength / 2;

	    for (int i = 0; i < numToRemove; i++){
		processedMsgs.removeFirst();
	    }
	}

        processedMsgs.addLast(t.getMessage());

    } // end addMessageToProcessedList


    private void addMessageToProcessedList(String message){

	if (processedMsgs.size() == historyLength){

	    int numToRemove = historyLength / 2;

	    for (int i = 0; i < numToRemove; i++){
		processedMsgs.removeFirst();
	    }
	}

        processedMsgs.addLast(message);

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