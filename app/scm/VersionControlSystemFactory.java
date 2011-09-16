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

/**
 * Factory class for creating VCSs
 */
public class VersionControlSystemFactory {

	/**
	 * VCS types supported
	 */
	public enum VersionControlSystemType {
		GIT, SVN
	}
	
	/**
	 * Get a VCS instance for the given type
	 * @param type VCS type
	 */
	public static VersionControlSystem getVersionControlSystem(final VersionControlSystemType type) throws Exception {
		switch(type) {
			case GIT:
				return new GitVersionControlSystem();
			case SVN:
				return new SubversionVersionControlSystem();
			default:
				throw new Exception("Unimplemented VCS");
		}
	}
}
