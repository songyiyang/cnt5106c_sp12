public enum ProtocolCommand {

    INSERT("INSERT", "%s %s %d %s"),
    DELETE("DELETE", "%s %s %d %s"),
    GET("GET", "%s %s %s"),
    GAMEOVER("GAMEOVER", null),
    TEST("TEST", null);

    private String command;
    private String payload;

    private static final String packetHeader = "%s %s";
    private static final String errorPayload = "ERROR %s";
    private static final String successPayload = "SUCCESS";

    private static final String sendHeader = "SND";
    private static final String receiveHeader = "RCV";

    ProtocolCommand(String _command, String _payload){
	command = _command;
	payload = _payload;
    }

    public String getCommand(){
	return command;
    }

    public static String createPacket(ProtocolCommand cmd,
			 String name, IPAddress address,
			 int direction, boolean error){

	String msg = null;
	String dir = null;

	if (direction == 0){
	    dir = sendHeader;
	}
	else {
	    dir = receiveHeader;
	}

	msg = String.format(packetHeader, dir, cmd.getCommand());

	switch (cmd){

	    case INSERT:

		break;

	    case DELETE:

		break;

	    case GET:

		break;

	    case GAMEOVER:

		break;

	    case TEST:

		break;
	} // end switch cmd

	return msg;

    } // end method createRecordPacketMessage


} // end enum ProtocolCommand