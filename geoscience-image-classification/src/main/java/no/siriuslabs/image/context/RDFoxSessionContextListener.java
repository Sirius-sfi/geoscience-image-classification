/*******************************************************************************
 * Copyright 2017 by the Department of Informatics (University of Oslo)
 * 
 *    This file is part of the Ontology Services Toolkit 
 *
 *******************************************************************************/
package no.siriuslabs.image.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author ernesto
 * Created on 29 Jan 2018
 *
 */
public class RDFoxSessionContextListener  implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(RDFoxSessionContextListener.class);
	
	public final static String RDFOX_SESSION = "session";
	
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		ServletContext sc = sce.getServletContext();
		
		LOGGER.info("SETTING UP CONTEXT FOR SESSIONS...");
		
		RDFoxSessionManager sessionManager = new RDFoxSessionManager();
		
		sc.setAttribute(RDFOX_SESSION, sessionManager);

		LOGGER.info("CONTEXT FOR SESSIONS DONE");
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		
		ServletContext sc = sce.getServletContext();
		
		RDFoxSessionManager session = (RDFoxSessionManager) sc
				.getAttribute(RDFOX_SESSION);

		LOGGER.info("DESTROYING CONTEXT FOR SESSIONS...");
		
		int n_sessions = session.getLoadedOntologies().size();
		
		session.clearAllOntologySessions();

		LOGGER.info("DONE. Removed " + n_sessions + " sessions.");
		
	}
	
	

}
