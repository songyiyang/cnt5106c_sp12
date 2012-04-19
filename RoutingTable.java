import java.util.TreeMap;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class RoutingTable
 *
 * @desc Acts as a routing table for the server. Lookups
 *       determine how to get to other servers.
 */
public class RoutingTable
{

    LinkedList<Record> records;
    TreeMap<String,RoutingEntry> entries;

    ReentrantLock tlock;
    ReentrantLock rlock;

    public RoutingTable() {

	neighbors = new LinkedList<Record>();
	entries = new TreeMap<String,RoutingEntry>();

	tlock = new ReetrantLock();
	rlock = new ReentrantLock();

    }


    /****************************************************


	methods to add/delete/find forwarding information


    *****************************************************/

    public void updateEntry(String server, int hopCount){



    }


    /****************************************************


	methods to add/delete/find records


    *****************************************************/

    /**
     * Add a new record to the list of records.
     */
    public boolean addRecord(Record record){

	boolean added = false;
	boolean duplicateExists = false;

	for (Record temp : records){
	    if (temp.getName().equals(record.getName())){
		duplicateExists = true;
		break;
	    }
	}

	if (!duplicateExists){
	    records.addLast(record);
	    added = true;
	}

	return added;

    } // end method addRecord

    /**
     * Delete the specified record.
     *
     * @param name
     *    The name of the record to delete.
     * @param ipAddress
     *    The IP address of the record to delete. May be
     *    null.
     *
     * @return
     *    TRUE if the record could be deleted, FALSE otherwise.
     */
    public boolean deleteRecord(String name, IPAddress ipAddress){

	boolean deleted = false;
	boolean match = false;

	Record deleteMe = null;
	String address = null;

	String addressTemp = null;

	    // Go through all the records
	for (Record temp : records){

	    match = false;

		// Try to match against the supplied name
	    match = temp.getName().equals(name);

		// If the supplied IP address isn't null, check
		// against it as well
	    if (ipAddress.getIPAddress() != null){
		address = ipAddress.getIPAddress();
		addressTemp = temp.getIPAddress().getIPAddress();
		match = match && addressTemp.matches(address);
	    }

		// Also check against supplied port number, if it
		// isn't == 0
	    if (ipAddress.getPort() > 0){
		match = match && (temp.getIPAddress().getPort()
					 == ipAddress.getPort());
	    }

		// If found, break from loop
	    if (match){
		deleteMe = temp;
		break;
	    }

	} // end foreach

	    // If a record was found, attempt to delete it
	if (deleteMe != null){
	    deleted = records.remove(deleteMe);
	}

	return deleted;

    } // end method deleteRecord


    public Record getRecord(String name){

	Record record = null;

	for (Record temp : records){
	    if (temp.getName().equals(name)){
		record = temp;
		break;
	    }
	}

	return record;

    }

    /**
     * Find records that match the given regular expressions.
     *
     * @param name
     *    The regular expression against which to compare
     *    the name field of records.
     * @param address
     *    The regular expression against which to compare
     *    the IP address field of records.
     *
     * @return
     *    A LinkedList of Record objects that match the given
     *    regular expressions.
     */
    public LinkedList<Record> findRecords(String name, String address){

	    // Parse the given regex for the address
	String addressRegex = IPAddress.parseIPAddressRegex(address);
	IPAddress ipRegex = new IPAddress(addressRegex);

	    // Create empty list in which to put matched records
	LinkedList<Record> matchedRecords = new LinkedList<Record>();

	    // Run checks against all records in the list
	for (Record temp : records){

	    if (temp.matches(name, ipRegex)){
		matchedRecords.addLast(temp);
	    } // end if temp.matches

	} // end foreach

	return matchedRecords;

    } // end method findRecords

} // end class RoutingTable