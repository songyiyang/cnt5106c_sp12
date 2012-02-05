import java.net.InetAddress;

/**
 * @desc Acts as a wrapper to contain
 *       different representations of an
 *       IP address in Java.
 */
public class IPAddress
{

    public String ipString;
    public InetAddress ipNetAddress;

    public IPAddress(){

	ipString = "";
	ipNetAddress = null;

    }

    public IPAddress(InetAddress address){

    }

} // end class IPAddress