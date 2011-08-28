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

package core;

import java.util.List;

import models.Application;
import play.Logger;
import play.Play;
import play.Play.Mode;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

/**
 * Bootstrap class for loading test data when running in DEV mode
 */
@OnApplicationStart
public class Bootstrap extends Job {

	@Override
	public void doJob() throws Exception {
		Logger.info("Bootstrapping Play AS");
		
		if(Play.mode == Mode.DEV) {
			Logger.info("Loading fixtures");
			Fixtures.deleteAllModels();
			Fixtures.loadModels("test-data.yml");

			final List<Application> applications = Application.all().fetch();
			for(final Application application : applications) {
				application.clean();
				application.checkout();
			}
		}
		
		Logger.info("Play AS started");
	}
}
