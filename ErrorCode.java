/**
 * @desc Defines errors that may occur in the
 *       execution of the system.
 *
 */
public enum ErrorCode
{

    FAIL_WHALE(72, "fail whale is failing, unknown error occurred"),
    PACKET_EXPLODED(777, "packet inexplicably blew up"),
    TIMEOUT(1973, "server's response timed out - it is wasting the "+
                  "hours in an offhand way"),
    RECORD_NOT_FOUND(404, "cannot find record"),
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

		// Record could not be found
	    case 404:

		ec = ErrorCode.RECORD_NOT_FOUND;
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