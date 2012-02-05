import java.net.InetAddress;

/**
 * @desc Acts as a wrapper to contain
 *       different representations of an
 *       IP address in Java.
 */
public class IPAddress
{

    private String ipAddress;
    private InetAddress ipNetAddress;
    private int port;

    public static String ipRegex = "^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])" +
		           "(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";

    public IPAddress(){

	ipAddress = null;
	ipNetAddress = null;
	port = 0;

    }

    public IPAddress(InetAddress _ipNetAddress){
	ipNetAddress = _ipNetAddress;
	ipAddress = ipNetAddress.getHostAddress();
    }

    public IPAddress(String _ipAddress){
	ipAddress = _ipAddress;
    }

    public IPAddress(String _ipAddress, int _port){
	ipAddress = _ipAddress;
	port = _port;
    }

    public IPAddress(InetAddress _ipNetAddress, int _port){
	ipNetAddress = _ipNetAddress;
	ipAddress = ipNetAddress.getHostAddress();
	port = _port;
    }


    public boolean matches(IPAddress _ipAddress){

	String ipToMatch = _ipAddress.getIPAddress();
	ipToMatch = ipToMatch.replaceAll("\\.","\\.");
	ipToMatch = ipToMatch.replaceAll("\\*","[0-9]");

	return this.ipAddress.matches(ipToMatch);

    }

    /**
     * Accessor methods
     */

    public String getIPAddress(){
	return ipAddress;
    }

    public InetAddress getIPNetAddress(){
	return ipNetAddress;
    }

    public int getPort(){
	return port;
    }

    public String toString(){

	StringBuffer output = new StringBuffer(ipAddress);

	if (port > 0){
	    output.append(":" + port);
	}

	return output.toString();

    }

} // end class IPAddress