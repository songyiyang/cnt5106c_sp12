/**
 * @class RoutingEntry
 *
 * @desc Contains information about a routing entry
 *       in the network.
 */
public class RoutingEntry
{

	// The node to which this entry directs
    private String node;
	// The next server in sequence to the node 
    private String next;
	// The hop count to the server
    private int hopCount;

	// Sentinal value that indicates the node is not reachable
    public static int UNREACHABLE_NODE = -1;

    public RoutingEntry(String _node, String _next) {

	node = _node;
	next = _next;
	hopCount = -1;

    }

    public RoutingEntry(String _node, String _next, int _hopCount) {

	node = _node;
	next = _next;
	hopCount = _hopCount;

    }

    public String getServer(){
	return server;
    }

    public String getNeighbor(){
	return neighbor;
    }

    public int getHopCount(){
	return hopCount;
    }

    public void setNext(String _neighbor){
	neighbor = _neighbor;
    }

    public void setHopCount(int _hopCount){
	hopCount = _hopCount;
    }


} // end class RoutingEntry