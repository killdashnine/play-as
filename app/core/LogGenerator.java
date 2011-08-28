package core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import play.jobs.Job;
import play.libs.F.Promise;

public class LogGenerator extends Job {

	private BufferedReader bufferedReader;
	private InputStreamReader inputStreamReader;
	private FileInputStream fileInputStream;
	
	public LogGenerator(final String filePath) throws Exception {
		fileInputStream = new FileInputStream (filePath);
		inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
		bufferedReader = new BufferedReader(inputStreamReader);
		bufferedReader.skip(fileInputStream.available()); // skip to end
	}
	
	public String doJobWithResult() throws Exception {
		while (true) {
		    final String line = bufferedReader.readLine();
		    if (line == null) {
		    	// timeout
		        Thread.sleep(500);
		    }
		    else {
		    	return line + "<br/>";
		    }
		}
	}
	
	public void close() throws IOException {
		fileInputStream.close();
	}
}
