/**
 * @desc Defines errors that may occur in the
 *       execution of the system.
 *
 */
public enum ErrorCode
{

    FAIL_WHALE(72, "fail whale is failing, unknown error occurred"),
    PACKET_EXPLODED(666, "packet inexplicably blew up"),
    TIMEOUT(1973, "server's response timed out. it is wasting the "+
                  "hours in an offhand way."),
    RECORD_NOT_FOUND(404, "cannot find record");

    private int number;
    private String message;

    ErrorCode(int _number, String _message){
	number = _number;
	message = _message;
    }

} // end enum ErrorCode