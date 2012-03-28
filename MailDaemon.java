import java.net.*;
import java.io.IOException;

/**
 * @desc This the thread that handles mail that may be
 *       sent to the client from a server.
 */

public class MailDaemon extends Thread
{

    String name;
    int port;
    IPAddress server;
    boolean shutdown;

    DatagramSocket mailSocket;
    DatagramPacket packet;

    public MailDaemon(String _name, int _port, IPAddress _server){

	name = _name;
	port = _port;
	server = _server;
	shutdown = false;

	try {
	    mailSocket = new DatagramSocket(port);
	    mailSocket.setSoTimeout(Protocol.MAX_TIMEOUT);
	}
	catch (SocketException e){
	    System.out.println("Daemon thread on port " + port +
			       " unable to open, printing stack trace...");
	    e.printStackTrace();
	}

	resetPacket();

    } // end constructor

    public void run(){

	IPAddress from;

	while (!shutdown){

		// Daemon blocks until mail arrives
		// May never unblock, if no mail is received!
	    try {
		from = Protocol.receive(mailSocket, packet);
	    }
	    catch (SocketTimeoutException e) {
		continue;
	    }

		// Have ClientProtocol remove the message
	    String message = ClientProtocol.extract(packet);

		// Put mail into the shared client mailbox
	    Client.addMailToQueue(message);

	    resetPacket();

	} // end while true

    } // end method run

    public void endDaemon(){
	shutdown = true;
    } // end method endDaemon

    public boolean matches(String _name, IPAddress _ip){
	return (name.equals(_name) && server.matches(_ip));
    } // end method matches

    private void resetPacket(){
	packet = new DatagramPacket(new byte[Protocol.PACKET_SIZE_LARGE],
				    Protocol.PACKET_SIZE_LARGE);
    } // end method resetPacket

} // end class MailDaemon