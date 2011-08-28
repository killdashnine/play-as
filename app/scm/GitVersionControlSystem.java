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

import core.ProcessManager;

/**
 * Implementation for a GIT Version Control System
 */
public class GitVersionControlSystem implements VersionControlSystem {

	@Override
	public String checkout(final String pid, final String gitUrl) throws Exception {
		final String checkoutPid = "git-checkout-" + pid;
		return ProcessManager.executeCommand(checkoutPid, "git clone " + gitUrl + " apps/" + pid);
	}
	
	@Override
	public String update(final String pid) throws Exception {
		final String checkoutPid = "git-pull-" + pid;
		return ProcessManager.executeCommand(checkoutPid, "git pull origin master", new File("apps/" + pid));
	}
	
	@Override
	public String cleanup(final String pid) throws Exception {
		final String checkoutPid = "git-checkout-" + pid;
		final StringBuffer output = new StringBuffer();
		output.append(ProcessManager.executeCommand(checkoutPid, "git --git-dir=apps/" + pid + "/.git --work-tree=apps/" + pid + " checkout -- conf/application.conf"));
		return output.toString(); 
	}
}
