import java.io.*;
import java.util.LinkedList;

public class RecordFile
{

    private String path;
    private File file;
    private boolean opened;

    public RecordFile(String _path){

	path = _path;
	file = null;
	opened = false;

	try {

	    file = new File(path);

	    if (!file.exists()){
		file.createNewFile();
	    }

	    opened = true;

	}
	catch (FileNotFoundException e) {
	    System.out.println("unable to open " + path);
	}
	catch (IOException e) {
	    System.out.println("problem with I/O, could not open " + path);
	}

    } // end constructor


    public void addRecordsToList(LinkedList<Record> records){

	String line = "";
	String[] tokens = null;

	int port = 0;
	String ip = null;
	Record record = null;

	try {

	    BufferedReader reader = new BufferedReader(new FileReader(file));

	    while ((line = reader.readLine()) != null){

		tokens = line.split("\\s*;\\s*");

		ip = tokens[1];
		port = Integer.parseInt(tokens[2]);

		record = new Record(tokens[0], new IPAddress(tokens[1], port));
		records.addLast(record);

	    } // end while line

	    reader.close();

	}
	catch (IOException e){ }

    } // end method addRecordsToList


    public void writeRecordsToFile(LinkedList<Record> records){

	String name = null;
	String ip = null;
	int port = 0;

	try {

	    BufferedWriter writer = new BufferedWriter(new FileWriter(file));

	    for (Record record : records){

		    // Get the record's constituent parts
		name = record.getName();
		ip = record.getIPAddress().getIPAddress();
		port = record.getIPAddress().getPort();

		    // Write out the record
		writer.write(String.format("%s; %s; %s", name, ip, "" + port));
		writer.newLine();
		writer.flush();

	    } // end for record

	    writer.close();

	}
	catch (IOException e) { }

    } // end if writeRecordsToFile

    public boolean opened(){
	return opened;
    } // end method opened

} // end class RecordFile