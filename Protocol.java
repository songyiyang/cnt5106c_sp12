import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * @desc Defines methods required by both client and
 *       server processes and protocol constants.
 *
 *       Note that protocol messages are defined by
 *       the ProtocolCommand class.
 *
 * @see ProtocolCommand.java
 */
public class Protocol
{

	// Default packet size is 1024 bytes
    public static final int PACKET_SIZE = 1024;
	// Large packet size is 1024 * 20 bytes
    public static final int PACKET_SIZE_LARGE = 1024 * 20;
	// Default timeout is 5 seconds
    public static final int MAX_TIMEOUT = 1000 * 5;

   /**
    * Remove the payload from the packet, removing any
    * extra whitespace in the process.
    *
    * @param packet
    *    The packet that contains the payload.
    *
    * @return
    *    The String representation of the payload sent
    *    to the client.
    */
    protected static String extractMessage(DatagramPacket packet){
	String message = new String(packet.getData());
	return message.trim();
    } // end method extractMessage

    /**
     * Receive a packet from some server.
     *
     * @param socket
     *    The DatagramSocket through which to talk to the
     *    server.
     * @param packet
     *    The packet through which the data is received.
     *
     * @return
     *    The IPAddress of the server from which the packet
     *    originated.
     */
    protected static IPAddress receive(DatagramSocket socket,
				       DatagramPacket packet)
				throws SocketTimeoutException {

	    IPAddress address = null;
	    boolean throwTimeoutException = false;

	    // Receive a packet sent to the socket
	try {
	    socket.receive(packet);
	    address = new IPAddress(packet.getAddress(), packet.getPort());
	}
	catch (SocketTimeoutException e){
	    throw new SocketTimeoutException();
	}
	catch (SocketException e){
	    // do something
	}	
	catch (IOException e){
	    // do something
	}


	return address;

    } // end method receive

    /**
     * Send a packet to some server.
     *
     * @param socket
     *    The DatagramSocket through which to talk to the
     *    server.
     * @param msg
     *    The message to send.
     * @param packet
     *    The packet through which the data is received.
     *
     * @return
     *    TRUE if the send operation was successful, FALSE
     *    otherwise.
     */
    protected static boolean send(DatagramSocket socket, String msg,
		                  IPAddress address){

	boolean success = false;

	byte[] msgBytes = msg.getBytes();
	DatagramPacket packet = null;

	try {

	    if (msgBytes.length <= PACKET_SIZE){
		packet = new DatagramPacket(msgBytes, msgBytes.length,
			    address.getIPNetAddress(), address.getPort());
		socket.send(packet);
		success = true;
	    }

	}
	catch (IOException e) {

	}

	return success;

    } // end method send

} // end class Protocol