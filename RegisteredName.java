public class RegisteredName
{

    private String name;
    private IPAddress mailAddress;

    public RegisteredName(String _name, IPAddress ip, int port){

	name = _name;
	mailAddress = new IPAddress(ip.getIPNetAddress(), port);

    } // end constructor

    public String getName(){
	return name;
    }


    public IPAddress getMailAddress(){
	return mailAddress;
    }

} // end class RegisteredName