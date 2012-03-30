import java.net.*;

public class Transaction
{

    private String message;
    private IPAddress ip;

    public Transaction(String _message, InetAddress _ip, int _port){
	message = _message;
	ip = new IPAddress(_ip, _port);
    }

    public String getMessage(){
	return message;
    }

    public IPAddress getIP(){
	return ip;
//	return new IPAddress(ip, port);
    }

} // end class Transaction