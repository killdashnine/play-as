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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.internal.runners.statements.RunAfters;

import models.Application;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

/**
 * TODO make paths configurable for binaries, so we become multi platform
 */
@Every("1s")
public class ProcessManager extends Job {
	
	public enum ProcessType {
		PLAY, COMMAND
	}
	
	static {
		//
		// THIS DOES NOT WORK WHEN JVM IS KILLED (example: kill -9)
		//
		// See: http://stackoverflow.com/questions/191215/how-to-stop-java-process-gracefully
		//
		final Runnable shutdownHook = new Runnable() {
			@Override
			public void run() {
				Logger.info("Shutdown hook called");
				synchronized (processes) {
					for(final Entry<String, Process> entry : processes.entrySet()) {
						try {
							Logger.info("Killing %s", entry.getKey());
							final Process process = entry.getValue();
							process.destroy();
							process.waitFor();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				Logger.info("Shutdown hook complete");
			}
		};
		
		Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
	}

	private static Map<String, Process> processes = new HashMap<String, Process>();
	
	public static Process executeProcess(final String pid, final String command) throws Exception {
		synchronized (processes) {
			
			if(processes.containsKey(pid)) {
				throw new Exception("pid: " + pid + " already in use");
			}
			
			final Process process = Runtime.getRuntime().exec(command);
			processes.put(pid, process);
			
			return process;
		}
	}
	
	@Override
	public void doJob() throws Exception {
		manageList();
		
		final List<Application> applications = Application.all().fetch();
		// check not running applications that should be running
		for(final Application application : applications) {
			if(application.enabled && application.checkedOut && !isProcessRunning(application.pid, ProcessType.PLAY)) {
				application.start();
			}
			else if(!application.enabled && isProcessRunning(application.pid, ProcessType.PLAY)) {
				application.stop();
			}
		}
	}
	
	public static String executeCommand(final String pid,
			final String command) throws Exception {
		return executeCommand(pid, command, true);
	}
	
	public static synchronized String executeCommand(final String pid,
			final String command, boolean log) throws Exception {
		final Process process = executeProcess(pid, command);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		final StringBuffer output = new StringBuffer();
	
		// asynchronous waiting here
		while (isProcessRunning(pid, ProcessType.COMMAND)) {
			String line = reader.readLine();
			while (line != null) {
				
				if(log) {
					Logger.info("command: %s", line);
				}
				
				output.append(line);
				line = reader.readLine();
			}
			
			String errorLine = errorReader.readLine();
			while(errorLine != null) {
				if(log) {
					Logger.error("command: %s", errorLine);
				}
				output.append(errorLine);
				errorLine = reader.readLine();
			}
		}
	
		if(log) {
			Logger.info("command %s completed", command);
		}
		
		// force removal
		synchronized(processes) {
			processes.remove(pid);
		}
	
		if (process.exitValue() != 0) {
			throw new Exception("command failed");
		}
	
		return output.toString();
	}

	public static void manageList() {
		/* pids to remove */
		final List<String> pids = new LinkedList<String>();
		
		synchronized (processes) {
			for(final Entry<String, Process> entry : processes.entrySet()) {
				try {
					final Process process = entry.getValue();
					final String pid = entry.getKey();
					final int status = process.exitValue();
					
					Logger.debug("Process with pid %s (%s) is not running anymore, removing from process list.", pid, status);
					pids.add(pid);
				}
				catch(IllegalThreadStateException e) {
					// still running! so ignore
				}
			}

			// remove all pids that have stopped
			for(final String pid : pids) {
				processes.remove(pid);
			}
		}
	}
	
	@Deprecated
	public static int killProcess(final String pid) throws Exception {
		synchronized (processes) {
			final Process process = processes.remove(pid);
			if(process != null) {
				// There currently is an issue with this as it kills the play python process
				// but not the spawned subprocess (JVM)
				process.destroy();
				process.waitFor();
				return process.exitValue();
			}
			else {
				throw new Exception("Unknown pid: " + pid);
			}
		}
	}
	
	public static boolean isProcessRunning(final String pid, final ProcessType type) throws Exception {
		synchronized (processes) {
			if(type == ProcessType.COMMAND) {
				final Process process = processes.get(pid);
				if(process != null) {
					try {
						process.exitValue(); // throws IllegalThreadStateException when task is still running
						return false;
					}
					catch(IllegalThreadStateException e) {
						return true;
					}
				}
				else {
					return false;
				}
			}
			else if(type == ProcessType.PLAY) {
				try {
					executeCommand("check-" + pid, "play pid apps/" + pid, false);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
			else {
				throw new Exception("Unhandeld process type: " + type);
			}
		}
	}
}
