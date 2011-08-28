package controllers;

import java.io.IOException;
import java.util.concurrent.Future;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.WebSocketController;
import core.LogGenerator;

public class LogController extends WebSocketController {

	/*public static void application(final Long id) {
		final Application application = Application.findById(id);
		if(application == null) {
			disconnect();
		}
		else {
			
		}
	}*/
	
	public static void manager() throws Exception {
		logToOutbound("logs/play-as.log");
	}

	private static void logToOutbound(String filePath) throws Exception, IOException {
		Logger.info("WebSocket client connected");
		final LogGenerator generator = new LogGenerator(filePath);
		while(inbound.isOpen()) {
			final Promise<String> promise = generator.now();
			final String data = await(promise);
			outbound.send(data);
	    }
		generator.close();
	}
}
