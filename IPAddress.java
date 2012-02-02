import java.net.INetAddress;

/**
 * @desc Acts as a wrapper to contain
 *       different representations of an
 *       IP address in Java.
 */
public class IPAddress
{

    public String ipString;
    public INetAddress ipNetAddress;

    public IPAddress(){

	ipString = "";
	ipNetAddress = null;

    }

    public IPAddress(INetAddress address){

    }

} // end class IPAddress