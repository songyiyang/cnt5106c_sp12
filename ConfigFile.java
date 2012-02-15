import java.io.*;
import java.util.LinkedList;

/**
 * @desc    ConfigFile acts as the server's
 *          means of reading important state information.
 */
public class ConfigFile
{

    private String path;
	// The port number for the connection
    private int port;
	// Set a flag to indicate that the file has been read
    private boolean hasBeenRead = false;

    public ConfigFile(String _path){

	path = _path;
	port = 0;

	readConfigFile();

    }

    /**
     * Accessor method
     */
    public int getPort(){
	return port;
    } // end method hasFileBeenRead


    /**
     * Return whether or not the config file has been read.
     */
    public boolean hasFileBeenRead(){
	return hasBeenRead;
    } // end method hasFileBeenRead

    /**
     * Reads a config file, given a proper path.
     *
     * @return
     *    TRUE if the file was successfully read in, FALSE
     *    otherwise. If FALSE is returned, the properties
     *    of the class are not guaranteed to be set.
     */
    public boolean readConfigFile(){

	    // Initially set file_read to false. Only
	    // set to true if the file has successfully
	    // been read and error is set to true.
	boolean fileRead = false;
	    // If there is an error, set a boolean for it
	boolean error = false;

	    // line and tokenized_line are used as buckets in which
	    // to read data
	String line = "";
	String[] tokenizedLine = new String[2];

	    // Attempt to read in the variables
	try {
		// Open the file and open a reader to it
	    File config = new File(path);
	    BufferedReader reader = new BufferedReader(new FileReader(config));

		// Iterate through each line of the file.
		// Set variables based on the data found on the
		// line.
	    while ((line = reader.readLine()) != null){

		    // Break up the line based on the placement
		    // of a colon and whitespace.
		tokenizedLine = line.split("\\s*=\\s*");

		    // Get the chunk size
		if (tokenizedLine[0].equals("port")){
		    port = Integer.parseInt(tokenizedLine[1]);
		} // end else if tokenizedLine[0]

		    // Else an unknown item was found; set error flag
		else {
		    error = true;
		    break;
		} // end else

	    } // end while line

	} // end try

	    // Generically catch any exception
	catch (Exception e) {
	    error = true;
	} // end catch e


	    // If the method got this far in execution without
	    // error, then file has been read
	if (!error){
	    fileRead = true;
	    hasBeenRead = true;
	} // end if error

	return fileRead;

    } // end method readConfigFile

} // end class ConfigFile