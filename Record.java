/**
 * @desc Defines a record in the system. The server
 *       maintains a list of records, which the
 *       client can modify via messages.
 */
public class Record
{

    private String name;
    private IPAddress ipAddress;
    private boolean linked;

    public Record(String _name, IPAddress _ipAddress){
	name = _name;
	ipAddress = _ipAddress;
	linked = false;
    }

    /**
     * Check to see if the given name and IP regular expressions
     * match those fields in the current record.
     *
     * @param _nameRegex
     *    The regular expression with which to test against the
     *    name field
     * @param _ipAddressRegex
     *    The regular expression with which to test against the
     *    ipAddress field
     *
     * @return
     *    TRUE if both fields match successfully, FALSE otherwise
     */
    public boolean matches(String _nameRegex, IPAddress _ipAddressRegex){

	_nameRegex = _nameRegex.replaceAll("\\*", ".*");

	boolean nameMatches = name.matches(_nameRegex);
	boolean addressMatches = ipAddress.matches(_ipAddressRegex);

        return (nameMatches && addressMatches);

    } // end method matches


    /**
     * Accessor methods
     */

    public String getName(){
	return name;
    }

    public IPAddress getIPAddress(){
	return ipAddress;
    }

    public boolean getLinked(){
	return linked;
    }


    /**
     * Mutator methods
     */

    public void setName(String _name){
	name = _name;
    }

    public void setIPAddress(IPAddress _ipAddress){
	ipAddress = _ipAddress;
    }

    public void setLinked(boolean _linked){
	linked = _linked;
    }

    /**
     * toString()
     */

    public String toString(){
	return name.concat(" " + ipAddress.toString());
    } // end method toString

} // end class Record