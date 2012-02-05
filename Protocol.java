import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

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

    public static final int PACKET_SIZE = 2048;

    protected static String extractMessage(DatagramPacket packet){
	String message = new String(packet.getData());
	return message.trim();
    } // end method extractMessage

    protected static IPAddress receive(DatagramSocket socket,
				       DatagramPacket packet){

	IPAddress address = null;

	try {
	    socket.receive(packet);
	    address = new IPAddress(packet.getAddress(), packet.getPort());
	}
	catch (IOException e){
	    // do something
	}

	return address;

    } // end method receive

    protected static int send(DatagramSocket socket, String msg,
		              IPAddress address){

	int success = 0;

	byte[] msgBytes = msg.getBytes();
	DatagramPacket packet = null;

	try {

	    if (msgBytes.length <= PACKET_SIZE){
		packet = new DatagramPacket(msgBytes, msgBytes.length,
			    address.getIPNetAddress(), address.getPort());
		socket.send(packet);
		success = 1;
	    }

	}
	catch (IOException e) {

	}

	return success;

    } // end method send

} // end class Protocol