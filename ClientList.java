import java.util.LinkedList;

public class ClientList
{

    private LinkedList<String> list;

    public ClientList(){
	list = new LinkedList<String>();
    }


    public void add(String name){

	if (!list.contains(name)){
	    list.addLast(name);
	}

    }

    public LinkedList<String> getList(){
	return list;
    }

    public int size(){
	return list.size();
    }

} // end class ClientList