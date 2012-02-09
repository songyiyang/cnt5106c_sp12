import java.io.*;
import java.net.*;
import java.util.LinkedList;

/**
 * @desc This the server process that communicates
 *       with some client.
 */
public class Server
{

//    private ConfigFile config;
    private static DatagramSocket socket;
    private static int port;

    private static LinkedList<Record> records = new LinkedList<Record>();


    public static void main(String[] args){

	DatagramPacket packetIn = null;
	byte[] dataIn = new byte[Protocol.PACKET_SIZE];

	IPAddress client = null;

	boolean wait = false;

	port = 1648;

	try {

	    socket = new DatagramSocket(port);
	    wait = true;

	    System.out.println("server located at IP " +
//			       socket.getInetAddress().getHostAddress() +
			       " and port " + port);

	    packetIn = new DatagramPacket(dataIn, dataIn.length);

	}
	catch (SocketException e) {
	    System.out.println("server could not open UDP port on port "
			       + port);
	}

	String system_msg = null;

	while (wait){

	    dataIn = new byte[Protocol.PACKET_SIZE];
	    packetIn = new DatagramPacket(dataIn, dataIn.length);

	    client = ServerProtocol.receive(socket, packetIn);
	    system_msg = ServerProtocol.parseMessage(socket, packetIn, client);

	    if (system_msg.matches("^.+ GAMEOVER$")){
		wait = false;
	    }

	} // end while wait


	try {
	    socket.close();
	}
	catch (Exception e){ }

    } // end main

    public static void addRecord(Record record){
	records.addLast(record);
    }

    public static boolean deleteRecord(String name, IPAddress ipAddress){

	boolean deleted = false;
	boolean match = false;

	Record temp = null;
	String address = null;

	for (temp : records){

	    match = false;

	    match = temp.getName().equals(name);

	    if (ipAddress.getIPAddress() != null){
		address = ipAddress.getIPAddress();
		match = match && temp.getIPAddress().matches(address);
	    }

	    if (ipAddress.getPort() > 0){
		match = match && (temp.getIPAddress().getPort() == ipAddress.getPort());
	    }

	    if (match){
		break;
	    }

	} // end for

	if (match){
	    deleted = records.remove(temp);
	}

	return deleted;

    } // end method deleteRecord

} // end class Server