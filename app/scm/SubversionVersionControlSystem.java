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

import java.io.File;

import play.Play;

import core.ProcessManager;

/**
 * Implementation for a GIT Version Control System
 */
public class SubversionVersionControlSystem implements VersionControlSystem {

	public String getFullSubversionPath() {
		final String path = Play.configuration.getProperty("path.svn");
		// return setting from application.conf or assume command is on the instance's path
		return path == null || path.isEmpty() ? "svn" : path;
	}
	
	@Override
	public String checkout(final String pid, final String url) throws Exception {
		final String checkoutPid = "svn-checkout-" + pid;
		return ProcessManager.executeCommand(checkoutPid, getFullSubversionPath() + " checkout " + url + " apps/" + pid, new StringBuffer());
	}
	
	@Override
	public String update(final String pid) throws Exception {
		final String checkoutPid = "svn-update-" + pid;
		return ProcessManager.executeCommand(checkoutPid, getFullSubversionPath() + " update", new StringBuffer(), new File("apps/" + pid));
	}
	
	@Override
	public String cleanup(final String pid) throws Exception {
		final String checkoutPid = "svn-revert-" + pid;
		final StringBuffer output = new StringBuffer();
		ProcessManager.executeCommand(checkoutPid, getFullSubversionPath() + " revert *", output);
		return output.toString(); 
	}
}
