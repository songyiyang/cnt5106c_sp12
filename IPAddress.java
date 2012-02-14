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

	// Regular expression for IP address
    public static final String ipRegex = "^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d"+
	     "|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";

	// Regular expression for IP address, allowing for 1-2
	// *s for each subnet
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


    /**
     * Check to see if this IP address matches the supplied
     * IPAddress regular expression.
     *
     * @param _ipAddress
     *    IPAddress that represents some regular expression.
     * 
     * @return
     *    TRUE if this IP address matches the regular expression,
     *    FALSE otherwise.
     */
    public boolean matches(IPAddress _ipAddress){
	String ipRegex = _ipAddress.getIPAddress();
	return ipAddress.matches(ipRegex);
    } // end method matches


    /**
     * Take a regular expression for an IP address and
     * convert it into something Java can use as a regex.
     *
     * @param ipRegex
     *    The String representation of the current regular
     *    expression
     *
     * @return
     *   The modified regular expression, as a String
     */
    public static String parseIPAddressRegex(String ipRegex){

	    // Break the IP address into 4 parts
	String[] tokens = ipRegex.split("\\.");

	    // The returned regex will be strict. Pattern must
            // match exactly.
	ipRegex = "^";

	String[] subnets = new String[tokens.length];

	    // Iterate through each part of the IP address
	for (int i = 0; i < tokens.length; i++) {

	    subnets[i] = tokens[i];

		// Case 1: match all possible numbers (ie, *)
	    if (subnets[i].equals("*")){
		subnets[i] = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])";
	    }

		// Case 2: match 2 digit number (*d or d*)
	    else if (subnets[i].length() == 2){
		if (subnets[i].charAt(0) == '*'){
		    subnets[i] = subnets[i].replaceFirst("\\*", "[1-9]");
		}
		if (subnets[i].matches("\\*")){
		    subnets[i] = subnets[i].replaceFirst("\\*", "[0-9]");
		}
	    }

		// Case 3: match 3 digit number (ie, **d)
	    else if (subnets[i].length() == 3){
		if (subnets[i].charAt(0) == '*'){
		    subnets[i] = subnets[i].replaceFirst("\\*", "[1-2]");
		}
		if (subnets[i].matches("\\*")){
		    subnets[i] = subnets[i].replace("\\*","[0-5]");
		}
	    }

	} // end for i

	    // Glue the pieces together
	ipRegex = ipRegex.concat(subnets[0] + "\\." + subnets[1] +
				 "\\." + subnets[2] + "\\." + subnets[3]);

	    // Specify that pattern must be tight match
	ipRegex = ipRegex.concat("$");

	return ipRegex;

    } // end method parseIPAddressRegex

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

    /**
     * toString()
     */

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

    } // end method toString

} // end class IPAddress