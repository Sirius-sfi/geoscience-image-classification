package no.siriuslabs.image;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

/**
 * Abstract class which provides basic structure and functionality for a generic application for annotating things.
 */
public abstract class AbstractAnnotationApplication extends WApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAnnotationApplication.class);

	private final ServletContext context;

	/**
	 * Creates a new application instance.
	 */
	protected AbstractAnnotationApplication(WEnvironment env) {
		super(env);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		context = getEnvironment().getServer().getServletContext();

		setTitle(getApplicationTitle());

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	public ServletContext getServletContext() {
		return context;
	}

	/**
	 * Returns the title of the application.
	 */
	public abstract String getApplicationTitle();

}
