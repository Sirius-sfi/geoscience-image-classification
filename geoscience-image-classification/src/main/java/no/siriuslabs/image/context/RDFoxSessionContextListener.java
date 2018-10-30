/*******************************************************************************
 * Copyright 2017 by the Department of Informatics (University of Oslo)
 * 
 *    This file is part of the Ontology Services Toolkit 
 *
 *******************************************************************************/
package no.siriuslabs.image.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;

/**
 *
 * @author ernesto
 * Created on 29 Jan 2018
 *
 */
public class RDFoxSessionContextListener  implements ServletContextListener {

	
	public final static String RDFOX_SESSION = "session";
	
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		ServletContext sc = sce.getServletContext();
		
		System.out.print("SETTING UP CONTEXT FOR SESSIONS...");
		
		RDFoxSessionManager sessionManager = new RDFoxSessionManager();
		
		sc.setAttribute(RDFOX_SESSION, sessionManager);
		
		System.out.println("DONE");
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		
		ServletContext sc = sce.getServletContext();
		
		RDFoxSessionManager session = (RDFoxSessionManager) sc
				.getAttribute(RDFOX_SESSION);
		
		System.out.print("DESTROYING CONTEXT FOR SESSIONS...");
		
		int n_sessions = session.getLoadedOntologies().size();
		
		session.clearAllOntologySessions();	
				
		System.out.println("DONE. Removed " + n_sessions + " sessions.");
		
	}
	
	

}
