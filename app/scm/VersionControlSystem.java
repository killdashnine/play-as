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

import models.Application;

/**
 * Interface for a Version Control System (VCS)
 *
 */
public interface VersionControlSystem {

	/**
	 * Checkout an application
	 * @param pid Program ID
	 * @param url URL to be checked out
	 */
	public String checkout(final String pid, final String url) throws Exception;
	
	/**
	 * Pull the latest changes from the remote VCS
	 * @param pid Program ID
	 */
	public String update(final String pid) throws Exception;
	
	/**
	 * Remove all changes made by the application server
	 * @param pid Program ID
	 */
	public String cleanup(final Application application) throws Exception;
}
