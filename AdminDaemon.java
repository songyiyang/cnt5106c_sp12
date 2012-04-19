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

    private LinkedList<Transaction> jobQueue;
    private LinkedList<String> processedMsgs;

    private static final int DEFAULT_MAX_HISTORY = 20;
    private static final long SLEEP_TIMER = 1000 * 30;

    public AdminDaemon(){

	id = 0;
	t = null;
	shutdown = false;
	historyLength = DEFAULT_MAX_HISTORY;

	jobQueue = new LinkedList<Transaction>();
	processedMsgs = new LinkedList<String>();

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


    private void processLinkCmd(String tranid){

	String[] tokens = t.getMessage().split("\\s+");
	String message = "";
	String args = "";

	Record record = Server.rtable.getRecord(tokens[2]);
	IPAddress rsp = null;

	if (t.getMessage().matches(".+LINK.+")){

		// First, send a message to the remote server, listing off
		// our routing information
	    args = tranid + " " + Server.rtable.getRoutingInfo();

	    message = ProtocolCommand.createPacket(ProtocolCommand.CTRL_CONNECT,
		      "", null, args, 0, null);

	    send(message, record.getIPAddress());

		// Then, receive a message from the remote server and 
		// update the routing table with the information
	    rsp = receive();

	    if (rsp != null){

		message = ClientProtocol.extract(packet);
		tokens = message.split("\\s+");

		boolean modified = false;
		String[] vector = tokens[3].split(";");

		    // routing table will perform update
		modified = Server.rtable.updateTable(record, vector);

		if (modified){
		    
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
	    Server.rtable.removeEntry(record);

	}

	resetPacket();

    } // end processLinkCmd


    private void processControlCmd(String tranid){

	String reply = "";
	String message = t.getMessage();
	String[] tokens = message.split("\\s+");
	String args = "";	

	IPAddress rsp;

	    // CTRL_SEND - remove a registered name from the list
	if (tokens[1].matches("CTRL_SEND")) {

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

	resetPacket();

    } // end processControlCmd


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