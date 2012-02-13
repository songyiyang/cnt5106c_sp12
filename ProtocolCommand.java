public enum ProtocolCommand {

    INSERT("INSERT"),
    DELETE("DELETE"),
    GET("GET"),
    GAMEOVER("GAMEOVER"),
    TEST("TEST"),
    TRANSMIT("TRANSMIT");

    private String command;

    private static final String packetHeader = "%s %s";
    private static final String errorPayload = "ERROR ";
    private static final String successPayload = "SUCCESS";

    private static final String sendHeader = "SND";
    private static final String receiveHeader = "RCV";

    ProtocolCommand(String _command){
	command = _command;
    }

    public String getCommand(){
	return command;
    }

    public static String createPacket(ProtocolCommand cmd,
			 String name, IPAddress address, String args,
			 int direction, ErrorCode error){

	String msg = null;
	String dir = null;
	boolean payloadExists = true;

	if (direction == 0){
	    dir = sendHeader;
	}
	else {
	    dir = receiveHeader;
	}

	msg = String.format(packetHeader, dir, cmd.getCommand());

	switch (cmd){


		// format: DIR INSERT name ip:port {SUCCESS | ERROR code}
	    case INSERT:

		msg = msg.concat(" " + name + " " + address.toString());

		break;

		// format: DIR DELETE name {null|ip}[:port]
		// {SUCCESS | ERROR code}

	    case DELETE:

		msg = msg.concat(" " + name + " " + address.toString());

		break;

		// format: DIR GET name ip {SUCCESS | ERROR code}
	    case GET:

		msg = msg.concat(" " + name + " " + address.toString());

		break;

		// format: DIR GAMEOVER {SUCCESS | ERROR code}
	    case GAMEOVER:

		    // No payload to send
		payloadExists = false;

		break;

		// format: DIR TEST {SUCCESS | ERROR code}
	    case TEST:

		    // No payload to send...yet
		payloadExists = false;

		break;

		// format: DIR TRANSMIT #sent #total {name ip:port}+
		// {SUCCESS | ERROR code}
	    case TRANSMIT:

		msg = msg.concat(" " + args);

		break;

	} // end switch cmd

	if (payloadExists && direction == 1){

	    if (error == null){
		msg = msg.concat(" " + successPayload);
	    }
	    else {
		msg = msg.concat(" " + errorPayload + error.getNumber());
	    }

	}

	return msg;

    } // end method createRecordPacketMessage


} // end enum ProtocolCommand