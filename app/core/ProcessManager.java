package core;

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
	
	static {
		//
		// THIS DOES NOT WORK WHEN JVM IS KILLED (example: kill -9)
		//
		// See: http://stackoverflow.com/questions/191215/how-to-stop-java-process-gracefully
		//
		final Runnable shutdownHook = new Runnable() {
			@Override
			public void run() {
				synchronized (processes) {
					for(final Entry<String, Process> entry : processes.entrySet()) {
						try {
							final Process process = entry.getValue();
							process.destroy();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
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
			if(application.enabled && application.checkedOut && !isProcessRunning(application.pid)) {
				application.start();
			}
		}
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
					
					Logger.info("Process with pid %s (%s) is not running anymore, removing from process list.", pid, status);
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
	
	public static int killProcess(final String pid) throws Exception {
		synchronized (processes) {
			final Process process = processes.remove(pid);
			if(process != null) {
				process.destroy();
				return process.exitValue();
			}
			else {
				throw new Exception("Unknown pid: " + pid);
			}
		}
	}
	
	public static boolean isProcessRunning(final String pid) {
		synchronized (processes) {
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
	}
}
