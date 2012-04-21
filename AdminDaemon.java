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

	    // While the thread is alive, keep looping
	while (!shutdown){

		// If no jobs exist, await work
	    if (!doJobsExist()){
		try {
		    sleep(SLEEP_TIMER);
		}
		catch (InterruptedException e){ }
	    }

		// Take a job from the queue and process it.
		// Nothing is done if there is no work.
	    processJob();

	} // end while !shutdown

    } // end method run


    /****************************************************


	methods to process jobs


    *****************************************************/


    public void addJobToQueue(Transaction transaction){

	synchronized(jobQueue){
	    jobQueue.addLast(transaction);
	}

    }

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


    private boolean doJobsExist(){

	boolean jobsExist = false;

	synchronized(jobQueue){
	    jobsExist = (jobQueue.size() > 0);
	}

	return jobsExist;

    }

    private void getUnprocessedJob(){

	Transaction temp;

	while (t == null && doJobsExist()){

	    temp = jobQueue.removeFirst();

	    if (!processedMsgs.contains(temp.getMessage())){
		t = temp;
	    }

	} // end while message

    } // end getUnprocessedJob


    private void processJob(){

	getUnprocessedJob();

	if (t == null){
	    return;
	}

	String tranid = id + "-" + Server.name;

	    // Handle a LIST request
	if (t.getMessage().matches(".+LIST.+")){
	    processListCmd(tranid);
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
System.out.println("reached the end");
    } // end processJob


    /****************************************************


	methods to perform certain jobs


    *****************************************************/



    private void processListCmd(String tranid){

	String[] tokens = t.getMessage().split("\\s+");
	String names = tokens[2];
	String servers = tokens[3];
	Record link = null;

	String args = "";
	String message = "";
	IPAddress rsp = null;

	ProtocolCommand cmd = ProtocolCommand.CTRL_LIST;

	    // If current server is selected, list of the clients
	    // on this server
	if (servers.equals("*") || servers.matches(Server.name)){
	    // Get all clients on current server
	}

	args = tranid + " ";

	    // If all servers will be hit, get active links and
	    // send out
	if (servers.equals("*")){

	    args += servers + " " + names;
	    message = ProtocolCommand.createPacket(cmd, "", null,
			     args, 0, null);


	    Record[] links = Server.rtable.getActiveLinks();

	    for (int i = 0; i < links.length; i++){
		send(message, link.getIPAddress());
		rsp = receive();
		resetPacket();		
	    }

	} // end if servers.equals

	    // If only certain servers are selected, then
	    // forward request to those servers
	else {

	    String[] serverList = tokens[3].split(",");

	    for (int i = 0; i < serverList.length; i++){

		    // Ignore this server if it is in the list
		if (serverList[i].equals(Server.name)){
		    continue;
		}

		    // Get the next link
		link = Server.rtable.getNextLink(serverList[i]);

		    // If the link was found, send message over link
		if (link != null){
		    args += serverList[i] + " " + names;
		    message = ProtocolCommand.createPacket(cmd, "", null,
			     args, 0, null);

		    send(message, link.getIPAddress());
		    rsp = receive();
		    resetPacket();
		}

	    } // end for i

	} // end else


    } // end processListCmd

    private void processSendCmd(String tranid){
/*
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
*/
    }


    private void processLinkCmd(String tranid){

	String[] tokens = t.getMessage().split("\\s+");
	String message = "";
	String args = "";

	Record record = Server.rtable.getRecord(tokens[2]);
	IPAddress rsp = null;

	Record[] links = null;
	boolean modified = false;

	if (t.getMessage().matches(".+LINK.+")){

		// First, send a message to the remote server, listing off
		// our routing information
	    args = " 0 " + Server.name + " " + Server.myIP  + " "
		   + Server.rtable;

	    message = ProtocolCommand.createPacket(
			     ProtocolCommand.CTRL_CONNECT, "", null, args,
			     0, null);

	    send(message, record.getIPAddress());

		// Then, receive a message from the remote server and 
		// update the routing table with the information
	    rsp = receive();

	    if (rsp != null){

		message = ClientProtocol.extract(packet);
		tokens = message.split("\\s+");

		    // Insert the record
		modified = Server.rtable.addEntry(record.getName());


		if (!tokens[4].equals("-")) {

		    String[] vector = tokens[4].split(";");

			// Routing table will perform update
		    modified = modified || Server.rtable.updateTable(
						record.getName(), vector);

		}

		    // If an update occurred, traverse neighbor
		    // links and tell them to update
		if (modified){
		    updateNeighbors();
		} // end if modified

	    } // end if rsp

	} // end if LINK

	    // Else it's an UNLINK command, remove data
	else {

	    args = " 0 " + Server.name;

		// First, disconnect from the remote server.
	    message = ProtocolCommand.createPacket(
			ProtocolCommand.CTRL_DISCONNECT, "",
			null, args, 0, null);

	    send(message, record.getIPAddress());
	    rsp = receive();
System.out.println("got response for UNLINK");
		// Now update router table
	    modified = Server.rtable.removeEntry(record.getName());

	    if (modified){
		updateNeighbors();
	    }

	} // end if..else

    } // end processLinkCmd


    private void processControlCmd(String tranid){

	Record record = null;

	String reply = "";
	String message = t.getMessage();
	String[] tokens = message.split("\\s+");
	String args = "";	

	IPAddress rsp;

	if (tokens[1].matches("CTRL_CONNECT")) {

	    args = " 0 " + Server.name + " " + Server.myIP + " "
		   + Server.rtable;

	    message = ProtocolCommand.createPacket(
			     ProtocolCommand.CTRL_CONNECT, "", null, args,
			     0, null);

	    String[] ipParts = tokens[4].split(":");
	    int port = Integer.parseInt(ipParts[1]);
	    IPAddress server = new IPAddress(ipParts[0], port);

	    send(message, t.getIP());

	    Server.rtable.addRecord(new Record(tokens[3], server, true));

	    boolean modified = false;

		// Add new router entry
	    modified = Server.rtable.addEntry(tokens[3]);

	    if (!tokens[5].equals("-")){
		String[] vector = tokens[4].split(";");

		    // Update table
		modified = modified || Server.rtable.updateTable(
					      tokens[3], vector);
	    }

	    if (modified){
		updateNeighbors();
	    }

	}

	else if (tokens[1].matches("CTRL_DISCONNECT")) {

	    args = " 0 " + Server.name;

	    message = ProtocolCommand.createPacket(
			     ProtocolCommand.CTRL_DISCONNECT, "", null, args,
			     0, null);

	    send(message, t.getIP());

		// Remove entry
	    boolean modified = Server.rtable.removeEntry(tokens[3]);

		// Update neighbors if necessary
	    if (modified){
		updateNeighbors();
	    }

	}

	else if (tokens[1].matches("CTRL_UPDATE")) {

	    record = Server.rtable.getRecord(tokens[3]);

	    args = " 0 " + Server.name + " " + Server.rtable;

	    message = ProtocolCommand.createPacket(
			     ProtocolCommand.CTRL_UPDATE, "", null, args,
			     1, null);

	    send(message, t.getIP());
System.out.println("confirmed update");
	    boolean modified = false;

	    if (!tokens[4].equals("-")){
		String[] vector = tokens[4].split(";");

		    // Update table
		modified = modified || Server.rtable.updateTable(
					      tokens[3], vector);
	    }
System.out.println(Server.rtable);
	    if (modified){
		updateNeighbors();
	    }

	}

	else if (tokens[1].matches("CTRL_LIST")){

	    ProtocolCommand cmd = ProtocolCommand.CTRL_LIST;
	    args = tokens[2] + " " tokens[3] + " " + tokens[4];
	    String message = ProtocolCommand.createPacket(cmd, "", null,
			     args, 0, null);


	}
/*
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
*/
    } // end processControlCmd


    private void updateNeighbors(){

	Record[] links = Server.rtable.getActiveLinks();

	String args = " 0 " + Server.name + " " + Server.rtable;
	String message = ProtocolCommand.createPacket(
			      ProtocolCommand.CTRL_UPDATE, "", null, args,
			      0, null);

	IPAddress rsp = null;

	if (links != null){

	    for (int i = 0; i < links.length; i++){
		send(message, links[i].getIPAddress());
		resetPacket();
	    } // end for i

	} // end if links

    }

    /****************************************************


	helper methods


    *****************************************************/

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