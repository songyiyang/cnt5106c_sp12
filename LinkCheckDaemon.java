import java.io.*;
import java.net.*;

public class LinkCheckDaemon extends Thread
{

    private static final int AWAIT_RESPONSE_TIMER = 1000 * 30;
    private static final int SLEEP_TIMER = 1000 * 30;

    private static DatagramSocket socket;
    private static DatagramPacket packet;

    public LinkCheckDaemon(){

	setDaemon(true);

	try {
	    socket = new DatagramSocket();
	    socket.setSoTimeout(AWAIT_RESPONSE_TIMER);
	}
	catch (SocketException e){ }

	resetPacket();

    }

    public void run(){

	    // Create TEST message that will be used to test
	    // for connectivity
	String message = ProtocolCommand.createPacket(ProtocolCommand.TEST, "", 
					 new IPAddress(), "", 0, null);

	while(true){

		// Sleep for awhile
	    try {
		sleep(SLEEP_TIMER);
	    }
	    catch (InterruptedException e){ }

		// Get all active links
	    Record[] activeLinks = Server.rtable.getActiveLinks();

		// If there are links, test them
	    if (activeLinks != null){

		IPAddress rsp = null;

		for (int i = 0; i < activeLinks.length; i++){

			// Send a TEST message to the link
		    send(message, activeLinks[i].getIPAddress());
		    rsp = receive();

			// If no response, set link to inactive
		    if (rsp == null){
			activeLinks[i].setLinked(false);
		    }

		} // end for i

	    } // end if activeLinks

	} // end while true

    } // end run

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

    private void resetPacket(){
	packet = new DatagramPacket(new byte[Protocol.PACKET_SIZE_LARGE],
				    Protocol.PACKET_SIZE_LARGE);
    } // end resetPacket


} // end class LinkCheckDaemon