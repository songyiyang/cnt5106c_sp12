import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;
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

	records = new LinkedList<Record>();
	entries = new TreeMap<String,RoutingEntry>();

	tlock = new ReentrantLock();
	rlock = new ReentrantLock();

    }


    /****************************************************


	methods to add/delete/find forwarding information


    *****************************************************/

    public boolean addEntry(String neighbor){

	boolean modified = false;
	RoutingEntry entry = null;

	if (entries.containsKey(neighbor)){

	    entry = entries.get(neighbor);

	    if (entry.getHopCount() != 1){
		entry.setNext(neighbor);
		entry.setHopCount(1);
		modified = true;
	    }

	}
	else {
	    entry = new RoutingEntry(neighbor, neighbor, 1);
	    entries.put(neighbor, entry);
	    modified = true;
	}


	return modified;

    }

    public boolean removeEntry(String neighbor){

	boolean modified = false;

	RoutingEntry entry = null;
	Set<String> keys = entries.keySet();

	    // Iterate through all the keys
	for (String node : keys){

	    entry = entries.get(node);

	    if (entry.getNext().equals(neighbor)){
		entry.setNext("-");
		entry.setHopCount(RoutingEntry.UNREACHABLE_NODE);
		modified = true;
	    }

	} // end foreach keys

	getRecord(neighbor).setLinked(false);
	

	return modified;

    }

    public Record getNextLink(String destination){

	Record link = null;
	RoutingEntry entry = null;

	if (entries.containsKey(destination)){
	    entry = entries.get(destination);
	    link = getRecord(entry.getNext());
	}

	return link;

    }

    public boolean updateTable(String neighbor, String[] vector){

	RoutingEntry entry = null;
	boolean modified = false;

	    // Individual entries will be tracked with vectorEntry
	String[] vectorEntry = null;

	    // Temporary variables for each vectorEntry
	String node = "";
	String next = "";
	int hopCount = 0;
	int myHopCount = 0;

	    // Loop through received entries and update the table as
	    // necessary
	for (int i = 0; i < vector.length; i++){

		// Get the three parts to an entry
	    vectorEntry = vector[i].split(",");

	    node = vectorEntry[0];
	    next = vectorEntry[1];
	    hopCount = Integer.parseInt(vectorEntry[2]);
	    myHopCount = hopCount + 1;

		// Ignore the other server's entry for the current
		// server
	    if (node.equals(Server.name)){
		continue;
	    }

		// If this server already knows about the given node,
		// then check to see if an update is needed
	    else if (entries.containsKey(node)){

		entry = entries.get(node);

		    // If node is unreachable from neighbor, check
		    // to see if next hop must be updated
		if (hopCount == RoutingEntry.UNREACHABLE_NODE){

			// If node was reached through the neighbor,
			// mark as unreachable
		    if (entry.getNext().equals(neighbor)){
			entry.setNext("-");
			entry.setHopCount(RoutingEntry.UNREACHABLE_NODE);
			modified = true;
		    }

		}

		    // If the given node is reachable from our neighbor,
		    // update the entry
		else if (entry.getHopCount() == RoutingEntry.UNREACHABLE_NODE) {

		    entry.setNext(neighbor);
		    entry.setHopCount(myHopCount);
		    modified = true;

		}

		    // If the given node is reachable from our neighbor
		    // and has a lower weight, update the entry
		else if (entry.getHopCount() > myHopCount){

		    entry.setNext(neighbor);
		    entry.setHopCount(myHopCount);
		    modified = true;

		}

	    } // end if entries.containsKey

		// Else the node hasn't been seen before. Must add entry.
	    else {

		if (hopCount == RoutingEntry.UNREACHABLE_NODE){
		    myHopCount = RoutingEntry.UNREACHABLE_NODE;
		}

		entry = new RoutingEntry(node, neighbor, myHopCount);
		entries.put(node, entry);
		modified = true;
	    }

	} // end for i


	return modified;

    } // end updateTable


    public Record[] getActiveLinks(){

	Record[] links = null;

	    // Need to build a list of active links to be used
	    // by the calling thread
	ArrayList<Record> list = new ArrayList<Record>();

	for (Record record : records){

	    if (record.getLinked()){
		    list.add(record);
	    }


	} // end if modified

	links = new Record[list.size()];
	int i = 0;

	    // Generate an array if active links found!
	for (Record temp : list){
	    links[i] = temp;
	    i++;
	}

	return links;

    }

    public String toString(){

	String table = "";

	int i = 0;
	int size = entries.size();

	RoutingEntry entry = null;
	Set<String> keys = entries.keySet();

	    // Iterate through all the keys
	for (String node : keys){

		// Get the entry and print out
	    entry = entries.get(node);
	    table += entry.toString();

		// Print out a ; if more records exist
	    if (i < size-1){
		table += ";";
	    }

	    i++;

	} // end foreach keys

	if (i == 0){
	    table = "-";
	}

	return table;

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

    public Record getRecord(IPAddress address){

	Record record = null;

	for (Record temp : records){
	    if (temp.getIPAddress().toString().equals(address.toString())){
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