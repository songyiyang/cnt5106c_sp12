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

    public static final String ipRegex = "^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d"+
	     "|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";

    public static final String ipRegexWildcard = "^(\\d|[1-9]\\d|1\\d{2}" +
	    "|2[0-4]\\d|25[0-5]|\\*\\d{0,2}|\\d\\*{1,2}|\\d\\*\\d|\\*\\d"+
	    "|\\*\\d\\*)(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5]"+
	    "|\\*\\d{0,2}|\\d{1,2}\\*|\\d\\*\\d|\\*\\d|\\*\\d\\*)){3}$";

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
	String ipRegex = _ipAddress.getIPAddress();
	return ipAddress.matches(ipRegex);
    } // end method matches

    public static String parseIPAddressRegex(String ipRegex){

	String[] tokens = ipRegex.split("\\.");
	ipRegex = "^";

	String[] subnets = new String[tokens.length];

	for (int i = 0; i < tokens.length; i++) {

	    subnets[i] = tokens[i];

	    if (subnets[i].equals("*")){
		subnets[i] = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])";
	    } // case: match all possible numbers
	    else if (subnets[i].length() == 2){
		if (subnets[i].charAt(0) == '*'){
		    subnets[i] = subnets[i].replaceFirst("\\*", "[1-9]");
		}
		if (subnets[i].matches("\\*")){
		    subnets[i] = subnets[i].replaceFirst("\\*", "[0-9]");
		}
	    } // case: match 2 digit number
	    else if (subnets[i].length() == 3){
		if (subnets[i].charAt(0) == '*'){
		    subnets[i] = subnets[i].replaceFirst("\\*", "[1-2]");
		}
		if (subnets[i].matches("\\*")){
		    subnets[i] = subnets[i].replace("\\*","[0-5]");
		}
	    } // case: match 3 digit number
	} // end for i

	ipRegex = ipRegex.concat(subnets[0] + "\\." + subnets[1] +
				 "\\." + subnets[2] + "\\." + subnets[3]);
	ipRegex = ipRegex.concat("$");

	return ipRegex;

    } // end parseIPAddressRegex

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

	StringBuffer output = new StringBuffer("");

	if (ipAddress == null){
	    output.append("null");
	}
	else {
	    output.append(ipAddress);
	}

	if (port > 0){
	    output.append(":" + port);
	}

	return output.toString();

    }

} // end class IPAddress