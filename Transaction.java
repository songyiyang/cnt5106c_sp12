public class Transaction
{

    private String message;
    private IPAddress ip;

    public Transaction(String _message, IPAddress _ip){
	message = _message;
	IPAddress ip = _ip;
    }

    public String getMessage(){
	return message;
    }

    public IPAddress getIP(){
	return ip;
    }

} // end class Transaction