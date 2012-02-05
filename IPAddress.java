import java.net.InetAddress;

/**
 * @desc Acts as a wrapper to contain
 *       different representations of an
 *       IP address in Java.
 */
public class IPAddress
{

    private InetAddress IPNetAddress;
    private int port;

    public IPAddress(){

	IPNetAddress = null;
	port = -1;

    }

    public IPAddress(InetAddress _IPNetAddress){
	IPNetAddress = _IPNetAddress;
    }

    public IPAddress(InetAddress _IPNetAddress, int _port){
	IPNetAddress = _IPNetAddress;
	port = _port;
    }

    public String getIPAddress(){
	return IPNetAddress.getHostAddress();
    }

    public InetAddress getIPNetAddress(){
	return IPNetAddress;
    }

    public int getPort(){
	return port;
    }

} // end class IPAddress