package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.WContainerWidget;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.FrontendServlet;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.services.FileService;

/**
 * Abstract class which provides common basic functionality such as API access for containers.
 */
public abstract class AbstractAnnotationContainer extends WContainerWidget {

	// TODO could possibly also provide: being a PropertyChangeListener
	// TODO could possibly also provide: a message line/field and corresponding methods to display messages - problem is integration into the layout...

	private final AbstractAnnotationApplication application;

	/**
	 * Constructor taking the main application and the parent container (to be passed to the super class).
	 */
	protected AbstractAnnotationContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(parent);
		this.application = application;
	}

	/**
	 * Returns the ontology session ID.
	 */
	protected String getSessionID() {
		return (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
	}

	// TODO API method should be "de-imaged"
	/**
	 * Returns the sessions's API service instance.
	 */
	protected ImageAnnotationAPI getImageAnnotationAPI() {
		return (ImageAnnotationAPI) application.getServletContext().getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY);
	}

	/**
	 * Returns the sessions's file service instance.
	 */
	protected FileService getFileService() {
		return (FileService) application.getServletContext().getAttribute(FrontendServlet.FILE_SERVICE_KEY);
	}

	// TODO remove ASAP
	public AbstractAnnotationApplication getApplication() {
		return application;
	}
}
