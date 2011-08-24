package scm;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import play.Logger;
import core.ProcessManager;

public class ScmUtils {

	public static String executeScmProcess(final String pid,
			final String command) throws Exception {
		final Process process = ProcessManager.executeProcess(pid, command);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		final StringBuffer output = new StringBuffer();

		// asynchronous waiting here
		while (ProcessManager.isProcessRunning(pid)) {
			String line = reader.readLine();
			while (line != null) {
				Logger.info("SCM: %s", line);
				output.append(line);
				line = reader.readLine();
			}
			
			String errorLine = errorReader.readLine();
			while(errorLine != null) {
				Logger.error("SCM: %s", errorLine);
				output.append(errorLine);
				errorLine = reader.readLine();
			}
		}

		Logger.info("SCM: command %s completed", command);

		if (process.exitValue() != 0) {
			throw new Exception("SCM command failed");
		}

		return output.toString();
	}
}
