package core;

import java.util.List;

import models.Application;
import play.Logger;
import play.Play;
import play.Play.Mode;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart()
public class Bootstrap extends Job {

	@Override
	public void doJob() throws Exception {
		Logger.info("Bootstrapping Play AS");
		
		if(Play.mode == Mode.DEV) {
			Logger.info("Loading fixtures");
			Fixtures.deleteAll();
			Fixtures.load("test-data.yml");

			final List<Application> applications = Application.all().fetch();
			for(final Application application : applications) {
				application.clean();
				application.checkout();
			}
		}
		
		Logger.info("Play AS started");
	}
}
