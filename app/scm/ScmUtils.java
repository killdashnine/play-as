/*
 * Copyright 2011 Matthias van der Vlies
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
