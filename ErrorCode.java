/**
 * @desc Defines errors that may occur in the
 *       execution of the system.
 *
 */
public enum ErrorCode
{


    FAIL_WHALE(72, "fail whale is failing, unknown error occurred"),
    NAME_PREVIOUSLY_REGISTERED(101, "someone else using name or ip"),
    NAME_NOT_FOUND (102, "name wasn't found, maybe"),
    PACKET_EXPLODED(777, "packet inexplicably blew up"),
    TIMEOUT(1973, "server's response timed out - it is wasting the "+
                  "hours in an offhand way"),
    RECORD_NOT_FOUND(404, "cannot find record"),
    SERVER_NOT_KNOWN(405, "can't find that server"),
    SERVER_NOT_LINKED(406, "we're not linked, dude"),
    SERVER_ALREADY_LINKED(407, "we're not linked, dude"),
    SERVER_BUSY(9876, "server is busy, go away!");

    private int number;
    private String message;

    ErrorCode(int _number, String _message){
	number = _number;
	message = _message;
    }

    public static ErrorCode getErrorCode(int code){

	ErrorCode ec = null;

	switch (code){

		// Unknown error occurred
	    case 72:

		ec = ErrorCode.FAIL_WHALE;
		break;

		// Name already taken at server
	    case 101:

		ec = ErrorCode.NAME_PREVIOUSLY_REGISTERED;
		break;

		// Can't find given name
	    case 102:

		ec = ErrorCode.NAME_NOT_FOUND;
		break;

		// Record could not be found
	    case 404:

		ec = ErrorCode.RECORD_NOT_FOUND;
		break;

		// Don't know what server user is mentioning
	    case 405:
		ec = ErrorCode.SERVER_NOT_KNOWN;
		break;

		// Not linked to given server
	    case 406:
		ec = ErrorCode.SERVER_NOT_LINKED;
		break;

		// Already linked to specified server
	    case 407:
		ec = ErrorCode.SERVER_ALREADY_LINKED;
		break;

		// Packet died horrible death
	    case 777:

		ec = ErrorCode.PACKET_EXPLODED;
		break;

		// Some sort of timeout occurred
	    case 1973:

		ec = ErrorCode.TIMEOUT;
		break;

		// Server is busy with something
	    case 9876:

		ec = ErrorCode.SERVER_BUSY;
		break;


	} // end switch code

	return ec;

    } // end method getErrorCode


    /**
     * Accessor methods
     */
    public int getNumber(){
	return number;
    }

    public String getMessage(){
	return message;
    }

} // end enum ErrorCode