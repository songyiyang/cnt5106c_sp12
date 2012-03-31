public class RegisteredName
{

    private String name;
    private IPAddress ip;
    private IPAddress mailAddress;

    public RegisteredName(String _name, IPAddress _ip, int port){

	name = _name;
	ip = _ip;
	mailAddress = new IPAddress(_ip.getIPNetAddress(), port);

    } // end constructor

    public String getName(){
	return name;
    }

    public IPAddress getIP(){
	return ip;
    }

    public IPAddress getMailAddress(){
	return mailAddress;
    }

} // end class RegisteredName