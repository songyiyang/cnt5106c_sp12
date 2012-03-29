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
    DatagramPacket packet;
    private LinkedList<String> jobQueue;
    private LinkedList<String> processedMsgs;

    private static long SLEEP_TIMER = 1000 * 30;

    public AdminDaemon(){

	shutdown = false;
	jobQueue = new LinkedList<String>();
	processedMsgs = new LinkedList<String>();

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

	    // Handle a REGISTER request
	else if (message.matches(".+REGISTER.+")){
	    // process REGISTER
	}

	    // Handle an UNREGISTER request
	else if (message.matches(".+UNREGISTER.+")){
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

    public void endDaemon(){
	shutdown = true;
    } // end endDaemon

    private void resetPacket(){
	packet = new DatagramPacket(new byte[Protocol.PACKET_SIZE_LARGE],
				    Protocol.PACKET_SIZE_LARGE);
    } // end resetPacket

} // end class MailDaemon