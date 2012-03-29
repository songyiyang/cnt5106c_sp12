import java.net.*;
import java.io.IOException;
import java.util.TreeMap;
import java.util.LinkedList;

/**
 * @desc This the thread that maintains network topology
 *       for a server. Each server will have one AdminDaemon
 *       running to test for neighboring links.
 */

public class AdminDaemon extends Thread
{

    boolean shutdown;
    int historyLength;
    DatagramPacket packet;

    private TreeMap<String,LinkedList<String>> clients;

    private LinkedList<Record> links;
    private LinkedList<String> jobQueue;
    private LinkedList<String> processedMsgs;

    private static final int DEFAULT_MAX_HISTORY = 20;
    private static final long SLEEP_TIMER = 1000 * 30;

    public AdminDaemon(){

	shutdown = false;
	historyLength = DEFAULT_MAX_HISTORY;

	links = new LinkedList<Record>();
	jobQueue = new LinkedList<String>();
	processedMsgs = new LinkedList<String>();

	clients = new TreeMap<String,LinkedList<String>>()

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

    private void addJob(String message){
	synchronized(jobQueue){
	    jobQueue.addLast(message);
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

	String message = getUnprocessedJob();

	if (message == null){
	    return;
	}

	    // Handle a LIST request
	if (message.matches(".+LIST.+")){
	    // process LIST
	}

	    // Handle a SEND request
	else if (message.matches(".+SEND.+")){
	    // process SEND
	}

	    // Handle registration requests
	else if (message.matches(".+(UN)?REGISTER.+")){
	    
	}

	    // Handle link requests
	else if (message.matches(".+(UN)?LINK.+")){
	    // process UNREGISTER
	}

	    // Handle some CONTROL message
	else {
	    // process CONTROL message

		// remember message in case someone sends this to you
	    AddMessageToProcessedList(message);

	}

    } // end processJob

    private String getUnprocessedJob(){

	String message = null;
	String temp = null;

	while (message != null && doJobsExist() > 0){

	    temp = jobsQueue.removeFirst();

	    if (!processedMsgs.contains(temp)){
		message = temp;
	    }

	} // end while message

	return message;

    } // end getUnprocessedJob


    private void processListCmd(){

    }

    private void processSendCmd(){

    }


    private void processRegisterCmd(){

    }

    private void processLinkCmd(){

    }


    private void processControlCmd(){

    }


    public void addLink(Record record){

	synchronized(links){
	    links.addLast(record);
	}

	processLinkCmd();

    }

    public void endDaemon(){
	shutdown = true;
    } // end endDaemon

    private void resetPacket(){
	packet = new DatagramPacket(new byte[Protocol.PACKET_SIZE_LARGE],
				    Protocol.PACKET_SIZE_LARGE);
    } // end resetPacket

} // end class MailDaemon