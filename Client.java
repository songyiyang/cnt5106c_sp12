import java.io.*;
import java.net.*;

/**
 * @desc This the client process that communicates
 *       with the server.
 */
public class Client
{

    private static IPAddress serverIP;
    private static DatagramSocket server;
    private static int port;

    private static BufferedReader reader;
    private static PrintWriter writer;

    public Client(){

	serverIP = null;
	server = null;
	serverPort = -1;

	reader = null;
	writer = null;

    } // end constructor

    public static void main(String[] args){

	boolean activeConnection = true;

	try {

	}
	catch (IOException e) {

	}
	catch (Exception e){
	    e.printStackTrace();
	}


	while (activeConnection){

	}

    } // end main method

    private static void ReadMsg(){

    }

    private static void SendMsg(){

    }

} // end class Client