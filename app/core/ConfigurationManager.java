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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import models.Application;
import models.ApplicationProperty;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import scm.GitVersionControlSystem;

/**
 * Manage individual application configurations and load GIT revision on application start
 */
@OnApplicationStart
public class ConfigurationManager extends Job {

	/**
	 * What index is used for container provided properties
	 */
	private static final int PRIORITY_MARGIN = 1000;

	/**
	 * Properties files use ISO-8859-1 encoding
	 */
	private static final String PROPERTIES_FILE_ENCODING = "ISO-8859-1";
	
	/**
	 * Comment prepended to exported configuration files
	 */
	private static final String PROPERTIES_FILE_COMMENT = "Generated by Play! Application Server";

	/**
	 * Get the path for the application.conf file
	 * @param application The application to generate the path for
	 */
	private static String getConfigurationFilePath(final Application application) {
		return "apps/" + application.pid + "/conf/application.conf";
	}
	
	/**
	 * Get the path for the log4j.properties file
	 * @param application The application to generate the path for
	 */
	private static String getLoggingConfigurationFilePath(final Application application) {
		return "apps/" + application.pid + "/conf/log4j.properties";
	}

	/**
	 * Generate configuration files for an application based on the configuration values in the database
	 * @param application The application to generate files for
	 */
	public static void generateConfigurationFiles(final Application application) throws IOException {
		Logger.info("Creating configuration file for application %s", application.pid);
		final Properties properties = new OrderedProperties();
		final Properties logProperties = new OrderedProperties();
		
		// #15 update log file 
		final ApplicationProperty logFileProperty = ApplicationProperty.findLogFileProperty(application);
		logFileProperty.value = "logs/" + application.pid + "_" + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".log";
		logFileProperty.save();
		
		final List<ApplicationProperty> applicationProperties = ApplicationProperty.find("application = ? order by priority", application).fetch();
		for(final ApplicationProperty property : applicationProperties) {
			// logging
			if(property.key.startsWith("log4j")) {
				logProperties.setProperty(property.key, property.value);
			}
			// application.conf
			else {
				properties.setProperty(property.key, property.value);
			}
		}
		final FileOutputStream outputStream = new FileOutputStream(getConfigurationFilePath(application));
		final FileOutputStream logOutputStream = new FileOutputStream(getLoggingConfigurationFilePath(application));
		properties.store(outputStream, PROPERTIES_FILE_COMMENT);
		logProperties.store(logOutputStream, PROPERTIES_FILE_COMMENT);
		outputStream.close();
		logOutputStream.close();
	}
	
	/**
	 * Read the current configuration from a string and load it into the database
	 * @param application The application to load the configuration for
	 * @param configuration A string with properties file data representation
	 */
	public static void readCurrentConfigurationFromString(final Application application, final String configuration) throws Exception {
		final OrderedProperties properties = new OrderedProperties();
		final InputStream inputStream = new ByteArrayInputStream(configuration.getBytes(PROPERTIES_FILE_ENCODING));
        properties.load(inputStream);    
        saveProperties(application, properties);
	}
	
	/**
	 * Read the current configuration and load it into the database
	 * @param application The application to load the configuration for
	 */
	public static void readCurrentConfigurationFromFile(final Application application) throws Exception {
		final OrderedProperties properties = new OrderedProperties();
        final FileInputStream inputStream = new FileInputStream(getConfigurationFilePath(application));
        properties.load(inputStream);    
        saveProperties(application, properties);
        
        // we will already add the logging and mode configuration here!
        new ApplicationProperty(application, 1, "application.mode", application.mode.toString()).save();
        
        // logging:
        new ApplicationProperty(application, 2, "log4j.rootLogger", "ERROR, Rolling").save();
        new ApplicationProperty(application, 3, "log4j.logger.play", "INFO").save();
        new ApplicationProperty(application, 4, "log4j.appender.Rolling", "org.apache.log4j.RollingFileAppender").save();
        new ApplicationProperty(application, 5, "log4j.appender.Rolling.File", "logs/" + application.pid + ".log").save();
        new ApplicationProperty(application, 6, "log4j.appender.Rolling.MaxFileSize", "128KB").save();
        new ApplicationProperty(application, 7, "log4j.appender.Rolling.MaxBackupIndex", "100").save();
        new ApplicationProperty(application, 8, "log4j.appender.Rolling.layout", "org.apache.log4j.PatternLayout").save();
        new ApplicationProperty(application, 9, "log4j.appender.Rolling.layout.ConversionPattern", "%d{ABSOLUTE} %-5p ~ %m%n").save();
	}
	
	/**
	 * Save updated configuration for an application
	 * @param application The application to save the configuration or
	 * @param properties The updated configuration properties
	 */
	public static void saveProperties(final Application application, final OrderedProperties properties) {
		Logger.info("config(%s): Reading application.conf", application.pid);
		
		// cache existing properties
		final Map<String, ApplicationProperty> existingProperties = new HashMap<String, ApplicationProperty>();
		
		if(application.properties != null) {
			for(final ApplicationProperty property : application.properties) {
				existingProperties.put(property.key, property);
			}
		}
		
		int priority = PRIORITY_MARGIN; // safe margin
		for(final Object rawKey : properties.keySet()) {
			final String key = (String) rawKey;
			final String value = (String) properties.getProperty(key);
			
			if("application.mode".equals(key) || key.startsWith("log4j")) {
				// ignore since the AS is responsible for generating this
				continue;
			}
			
			ApplicationProperty property = existingProperties.get(key);
			
			if(property == null) {
				// new property
				property = new ApplicationProperty();
				property.application = application;
				property.key = key;
			}
			else {
				existingProperties.remove(key);
			}
			
			Logger.info("config(%s): %s = %s", application.pid, key, value);
			
			property.value = value;
			property.priority = priority;
			property.save();
			priority++;
		}
		
		// remove deleted properties
		if(existingProperties.size() > 0) {
			for(final ApplicationProperty property : existingProperties.values()) {
				if("application.mode".equals(property.key) || property.key.startsWith("log4j")) {
					// ignore since the AS is responsible for generating this
					continue;
				}
				
				property.delete();
				Logger.info("config(%s): deleted %s", application.pid, property.key);
			}
		}
	}
	
	@Override
	public void doJob() throws Exception {
		try {
			final String revision = ProcessManager.executeCommand("git-describe", GitVersionControlSystem.getFullGitPath()
					+ " describe", new StringBuffer(), false, null, false);
			Play.configuration.setProperty("git.revision", revision);
			Logger.info("Play! Application Server revision %s", revision);
		}
		catch(Exception e) {
			Play.configuration.setProperty("git.revision", "unknown");
			Logger.info("Play! Application Server revision could not be determined");
		}
	}
}
