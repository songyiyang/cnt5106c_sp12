/**
 * @desc Defines a record in the system. The server
 *       maintains a list of records, which the
 *       client can modify via messages.
 */
public class Record
{

    private String name;
    private IPAddress ipAddress;

    public Record(String _name, IPAddress _ipAddress){
	name = _name;
	ipAddress = _ipAddress;
    }

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


    /**
     * Mutator methods
     */

    public void setName(String _name){
	name = _name;
    }

    public void setIPAddress(IPAddress _ipAddress){
	ipAddress = _ipAddress;
    }


    public String toString(){
	return name.concat(" " + ipAddress.toString());
    }

} // end class Record