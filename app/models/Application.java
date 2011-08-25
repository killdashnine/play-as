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

package models;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;

import play.Logger;
import play.Play.Mode;
import play.data.validation.Required;
import play.db.jpa.Model;
import scm.ScmUtils;
import scm.VersionControlSystemFactory;
import scm.VersionControlSystemFactory.VersionControlSystemType;
import core.PlayUtils;
import core.ProcessManager;

@Entity
@Table(name="applications")
public class Application extends Model {
	
	@Column(updatable = false, unique = true, nullable = false)
	public String pid;
	
	@Column(updatable = false, nullable = false)
	@Required
	public VersionControlSystemType vcsType;
	
	@Column(updatable = false, nullable = false)
	@Required
	public String vcsUrl;
	
	@Required
	public Boolean checkedOut = false;
	
	@Required
	public Boolean enabled;
	
	@Column(updatable = true, nullable = false)
	@Required
	public Mode mode;
	
	/**
	 * Configuration properties used for application.conf generation
	 */
	@OneToMany(fetch=FetchType.EAGER, mappedBy="application")
	public Set<ApplicationProperty> properties = new HashSet<ApplicationProperty>();
		
	/**
	 * Start the application
	 * @param force Force start?
	 */
	public void start() throws Exception {
		if(!enabled) {
			throw new Exception("Can not start disabled application " + pid);
		}
		else if(isRunning()) {
			throw new Exception("Application " + pid + " is already running.");
		}
		
		// generate application.conf
		PlayUtils.generateLogFile(this);
		
		ProcessManager.executeProcess(pid, "play run apps/" + pid);
		Logger.info("Started %s", pid);
	}
	
	/**
	 * Stop the application
	 */
	public void stop() throws Exception {
		if(!isRunning()) {
			Logger.warn("Not running application %s is being stopped." , pid);
		}
		
		ProcessManager.killProcess(pid);
		Logger.info("Stopped %s", pid);
	}
	
	public void restart() throws Exception {
		stop();
		start();
	}
	
	@Transient
	public boolean isRunning() throws Exception {
		if(!checkedOut) {
			throw new Exception("Application " + pid + " has not yet been checked out from SCM");
		}
		
		return ProcessManager.isProcessRunning(pid);
	}
	
	/**
	 * Pull most recent version from SCM
	 * @throws Exception 
	 */
	public void pull() throws Exception {
		if(isRunning()) {
			// stop the application
			stop();
		}
		
		// cleanup working directory
		VersionControlSystemFactory.getVersionControlSystem(vcsType).cleanup(pid);
		
		// pull changes from git
		VersionControlSystemFactory.getVersionControlSystem(vcsType).update(pid);
		
		// start the application
		start(); 
	}

	/**
	 * Fetch application from SCM for the first time
	 */
	public void checkout() throws Exception {
		if(checkedOut) {
			throw new Exception("Application " + pid + " is already checked out");
		}
		
		VersionControlSystemFactory.getVersionControlSystem(vcsType).checkout(pid, vcsUrl);
		
		checkedOut = true;
		save();
		
		// TODO load properties from existing application.conf file
	}
	
	public void clean() throws Exception {
		Logger.info("Removing SCM checkout for %s", pid);
		
		try {
			stop();
		}
		catch(Exception e) {
			// ignore
		}
		
		FileUtils.deleteDirectory(new File("apps/" + pid));

		checkedOut = false;
		save();
	}	
}
