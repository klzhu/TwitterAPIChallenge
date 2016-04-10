package com.vinechallenge.servlet.vine_api_servlet;

import java.io.File;
import java.util.Optional;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/*
 * Creates instance of tomcat and launches our application
 */
public class ServletDriver {

    public static final Optional<String> port = Optional.ofNullable(System.getenv("PORT"));
    public static void main(String[] args) throws ServletException, LifecycleException {
        
        Tomcat tomcat = new Tomcat();     
        tomcat.setPort(Integer.valueOf(port.orElse("8080") ));
        
        File base = new File(System.getProperty("java.io.tmpdir"));
        Context rootCtx = tomcat.addContext("", base.getAbsolutePath());
        Tomcat.addServlet(rootCtx, "VineChallengeServlet", new VineChallengeServlet());
        
        rootCtx.addServletMapping("/statuses", "VineChallengeServlet");
        
        tomcat.start();
        tomcat.getServer().await();
        
    }
}
