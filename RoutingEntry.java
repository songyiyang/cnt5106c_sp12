/**
 * @class RoutingEntry
 *
 * @desc Contains information about a routing entry
 *       in the network.
 */
public class RoutingEntry
{

    private String server;
    private String neighbor;
    private int hopCount;

	// Sentinal value that indicates the node is not reachable
    public static int UNREACHABLE_NODE = -1;

    public RoutingEntry(String _server, String _neighbor) {

	server = _server;
	neighbor = _neighbor;
	hopCount = -1;

    }

    public RoutingEntry(String _server, String _neighbor, int _hopCount) {

	server = _server;
	neighbor = _neighbor;
	hopCount = _hopCount;

    }

    public void setNeighbor(String _neighbor){
	neighbor = _neighbor;
    }

    public void setHopCount(int _hopCount){
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

} // end class RoutingEntry