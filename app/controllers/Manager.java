package controllers;

import java.util.List;

import play.mvc.Controller;

public class Manager extends Controller {

    public static void index() {
    	final List<Application> applications = models.Application.all().fetch();
        render(applications);
    }

}