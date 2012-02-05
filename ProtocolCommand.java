public enum ProtocolCommand {

    INSERT("INSERT %s %s %d"),
    DELETE("DELETE %s %s %d"),
    GET("GET %s %s"),
    KABOOM("KABOOM"),
    TEST("SANCHO");

    private String msgFormat;

    ProtocolCommand(String _msgFormat){
	msgFormat = _msgFormat;
    }

    public String getMessageFormat(){
	return msgFormat;
    }

    public static String createRecordPacketMessage(ProtocolCommand cmd,
			 String name, IPAddress address){

	String msg = null;

	switch (cmd){

	    case INSERT:

		break;

	    case DELETE:

		break;

	    case GET:

		break;

	    case KABOOM:

		break;

	    case TEST:

		break;
	} // end switch cmd

	return msg;

    } // end method createRecordPacketMessage


} // end enum ProtocolCommand