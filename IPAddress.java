import java.net.InetAddress;

/**
 * @desc Acts as a wrapper to contain
 *       different representations of an
 *       IP address in Java.
 */
public class IPAddress
{

    private String IPAddress;
    private InetAddress IPNetAddress;
    private int port;

    public IPAddress(){

	IPAddress = null;
	IPNetAddress = null;
	port = 0;

    }

    public IPAddress(InetAddress _IPNetAddress){
	IPNetAddress = _IPNetAddress;
    }

    public IPAddress(String _IPAddress){
	IPAddress = _IPAddress;
    }

    public IPAddress(String _IPAddress, int _port){
	IPAddress = _IPAddress;
	port = _port;
    }

    public IPAddress(InetAddress _IPNetAddress, int _port){
	IPNetAddress = _IPNetAddress;
	IPAddress = IPNetAddress.getHostAddress();
	port = _port;
    }

    public String getIPAddress(){
	return IPAddress;
    }

    public InetAddress getIPNetAddress(){
	return IPNetAddress;
    }

    public int getPort(){
	return port;
    }

    public String toString(){

	StringBuffer output = new StringBuffer(IPAddress);

	if (port > 0){
	    output.append(":" + port);
	}

	return output.toString();

    }

} // end class IPAddress