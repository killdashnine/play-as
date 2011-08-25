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


package controllers;

import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation;

import core.ProcessManager;
import models.Application;
import play.data.validation.Valid;
import play.mvc.Controller;

public class ApplicationController extends Controller {

	public static void show(final Long id) {
		final Application application = Application.findById(id);
		if(application == null) {
			notFound();
		}
		else {
			render(application);
		}
	}
	
	public static void edit() {
		
	}
	
	public static void create(@Valid final Application application) throws Exception {
		if(!validation.hasErrors()) {
			application.enabled = false;
			application.checkedOut = false;
			application.clean(); // override checkout if already exists
			application.checkout();
		}
		else {
			params.flash();
			validation.keep();
		}

		ManagerController.index();
	}
	
	public static void start(final Long id) throws InterruptedException, Exception {
		final Application application = Application.findById(id);
		
		if(application == null) {
			notFound();
		}
		else {
			application.enabled = true;
			application.save();
			
			ProcessManager.waitForCompletion(application);
			
			ManagerController.index();
		}
	}

	public static void stop(final Long id) throws InterruptedException, Exception {
		final Application application = Application.findById(id);
		
		if(application == null) {
			notFound();
		}
		else {
			application.enabled = false;
			application.save();
			
			ProcessManager.waitForCompletion(application);
			
			ManagerController.index();
		}
	}
	
	public static void update(final Long id) throws Exception {
		final Application application = Application.findById(id);
		
		if(application == null) {
			notFound();
		}
		else {
			application.pull();
			ManagerController.index();
		}
	}
}
