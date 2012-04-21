public enum ProtocolCommand {

    INSERT("INSERT"),
    DELETE("DELETE"),
    GET("GET"),
    GAMEOVER("GAMEOVER"),
    TEST("TEST"),
    TRANSMIT("TRANSMIT"),
    LINK("LINK"),
    UNLINK("UNLINK"),
    REGISTER("REGISTER"),
    UNREGISTER("UNREGISTER"),
    LIST("LIST"),
    SEND("SEND"),
    SEND_N("SEND_N"),
    SEND_RT("SEND_RT"),

    CTRL_CONNECT("CTRL_CONNECT"),
    CTRL_DISCONNECT("CTRL_DISCONNECT"),
    CTRL_UPDATE("CTRL_UPDATE"),
    CTRL_LIST("CTRL_LIST"),
    CTRL_SEND("CTRL_SEND"),
    CTRL_SEND_N("CTRL_SEND_N"),
    CTRL_SEND_F("CTRL_SEND_F");

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

	    // DIR == SND
	if (direction == 0){
	    dir = sendHeader;
	}

	    // DIR == RCV
	else {
	    dir = receiveHeader;
	}

	    // Packet message initially looks like "{SND|RCV} COMMAND"
	msg = String.format(packetHeader, dir, cmd.getCommand());

	switch (cmd){


		// format: DIR INSERT name ip:port {SUCCESS | ERROR code}
	    case INSERT:

		if (direction == 0){
		    msg = msg.concat(" " + name + " " + address.toString());
		}

		break;

		// format: DIR DELETE name {null|ip}[:port]
		// {SUCCESS | ERROR code}

	    case DELETE:

		if (direction == 0){
		    msg = msg.concat(" " + name + " " + address.toString());
		}

		break;

		// format: DIR GET name ip [#matches] {SUCCESS | ERROR code}
	    case GET:

		if (direction == 0){
		    msg = msg.concat(" " + name + " " + address.toString());
		}
		else {
		    msg = msg.concat(" " + args);
		}

		break;

		// format: DIR GAMEOVER [MSG]{SUCCESS | ERROR code}
	    case GAMEOVER:

		    // While no data is sent, in future server
		    // may send error code. For now, server
		    // will always send SUCCESS with this
		    // message.
		payloadExists = true;

		msg = msg.concat(" " + args);

		break;

		// format: DIR TEST {SUCCESS | ERROR code}
	    case TEST:

		    // No payload to send...yet
		payloadExists = false;

		if (direction == 0){
		    msg = msg.concat(" " + address.toString()); 
		}

		break;

		// format: DIR TRANSMIT {YAH|NAW} {name ip:port}+
		// {SUCCESS | ERROR code}
	    case TRANSMIT:

		if (direction == 0){
		    msg = msg.concat(" " + args);
		}

		break;

		// format: DIR LINK name {SUCCESS | ERROR code}
	    case LINK:

		if (direction == 0){
		    msg = msg.concat(" " + name);
		}

		break;

		// format: DIR UNLINK name {SUCCESS | ERROR code}
	    case UNLINK:

		if (direction == 0){
		    msg = msg.concat(" " + name);
		}

		break;

		// format: DIR REGISTER name port {SUCCESS | ERROR code}
	    case REGISTER:

		msg = msg.concat(" " + name + " " + args);

		break;

		// format: DIR UNREGISTER name {SUCCESS | ERROR code}
	    case UNREGISTER:

		if (direction == 0){
		    msg = msg.concat(" " + name);
		}

		break;

		// format: DIR SEND clients servers message
		// {SUCCESS | ERROR code}
	    case SEND:

		if (direction == 0){
		    msg = msg.concat(" " + args);
		}

		break;

		// format: DIR LIST clients servers [names]
		// {SUCCESS | ERROR code}
	    case LIST:

		msg = msg.concat(" " + args);

		break;

		// format: DIR CTRL_CONNECT 0 name ip:port 
		//         {[server,next,hopCount]+} {SUCCESS | ERROR code}
	    case CTRL_CONNECT:

		payloadExists = true;
		msg = msg.concat(" " + args);

		break;

		// format: DIR CTRL_DISCONNECT 0 name {SUCCESS | ERROR code}
	    case CTRL_DISCONNECT:

		payloadExists = true;
		msg = msg.concat(" " + args);

		break;

		// format: DIR CTRL_UPDATE 0 name {[server,next,hopCount]+}
		//         {SUCCESS | ERROR code}
	    case CTRL_UPDATE:

		payloadExists = true;
		msg = msg.concat(" " + args);

		break;


		// format: DIR CTRL_LIST tid servers clients
	    case CTRL_SEND:

		msg = msg.concat(" " + args);


		// format: DIR CTRL_SEND tid client server message
	    case CTRL_SEND:

		msg = msg.concat(" " + args);

		// format: DIR CTRL_SEND_N tid client servers 
	    case CTRL_SEND_N:

		msg = msg.concat(" " + args);


		// format: DIR CTRL_SEND tid client servers
	    case CTRL_SEND_F:

		msg = msg.concat(" " + args);


	} // end switch cmd


	    // Add "SUCCESS" or "ERROR code" to message
            // if this is a RCV message and there is a payload
	if (payloadExists && direction == 1){

		// If no error, then success
	    if (error == null){
		msg = msg.concat(" " + successPayload);
	    }

		// Else error occurred, print out what happened
	    else {
		msg = msg.concat(" " + errorPayload + error.getNumber());
	    }

	} // end if payloadExists

	return msg;

    } // end method createRecordPacketMessage


} // end enum ProtocolCommand