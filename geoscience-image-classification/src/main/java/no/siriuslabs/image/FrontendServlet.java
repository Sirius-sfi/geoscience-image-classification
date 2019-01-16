package no.siriuslabs.image;

import eu.webtoolkit.jwt.Configuration;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WtServlet;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.context.RDFoxSessionContextListener;
import no.siriuslabs.image.services.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;

/**
 * Servlet hosting the application.
 */
public class FrontendServlet extends WtServlet {

	private static final long serialVersionUID = 1578912809839556880L;

	private static final Logger LOGGER = LoggerFactory.getLogger(FrontendServlet.class);

	public static final String FILE_SERVICE_KEY = "fileService";
	public static final String IMAGE_ANNOTATION_API_KEY = "imageAnnotationAPI";
	public static final String SESSION_ID_KEY = "sessionID";

	public static final String WORK_DIRECTORY = "workDirectory";
	public static final String EXTERNAL_DIRECTORY_SET = "externalDirectorySet";
	

	@Override
	public WApplication createApplication(WEnvironment wEnvironment) {
		LOGGER.info("creating application...");
		alterConfiguration();

		createServices();

		final FrontendApplication application = new FrontendApplication(wEnvironment);
		initializeApplicationWithStylesheets(application);

		LOGGER.info("application ready");
		return application;
	}

	private void alterConfiguration() {
		LOGGER.info("setting configuration...");
		setConfiguration(new Configuration() {
			{
				// alter max. request size to enable file uploads up to...
				setMaximumRequestSize(1024*1024 * 20);
			}
		});
	}

	private void createServices() {
		LOGGER.info("creating services...");

		RDFoxSessionManager session = (RDFoxSessionManager) getServletContext().getAttribute(RDFoxSessionContextListener.RDFOX_SESSION);
		ImageAnnotationAPI icg = new ImageAnnotationAPI(session);

		String workDirectory = System.getProperty("workDirectory");
		boolean externalDirectorySet = workDirectory != null;
		getServletContext().setAttribute(EXTERNAL_DIRECTORY_SET, externalDirectorySet);
		LOGGER.info("system property workDirectory was: {}", workDirectory);
		String absoluteWebPath = externalDirectorySet ? workDirectory : getServletContext().getRealPath("/");
		getServletContext().setAttribute(WORK_DIRECTORY, absoluteWebPath);

		//Load default ontology
		String ontology_path = getServletContext().getInitParameter("ontology-path");
		String annotations_path = getServletContext().getInitParameter("annotations-path");
		
		String protocol = "file:";
		if (!absoluteWebPath.startsWith("/"))
			protocol+="/";
		
		String sessionID = protocol + absoluteWebPath + ontology_path;
		LOGGER.info("sesionID is: {}", sessionID);
		
		//New session with ontology and data (available annotations)
		icg.createNewSession(
				sessionID,
				absoluteWebPath + annotations_path);
		//end load default ontology
		
		getServletContext().setAttribute(IMAGE_ANNOTATION_API_KEY, icg);
		getServletContext().setAttribute(SESSION_ID_KEY, sessionID);

		FileService fileService = new FileService(getServletContext());
		getServletContext().setAttribute(FILE_SERVICE_KEY, fileService);

		// if an external directory was set, copy all images from there to the server's local image directory
		if(externalDirectorySet) {
			fileService.synchronizeImageDirectories();
		}

		LOGGER.info("services ready");
	}

	private void initializeApplicationWithStylesheets(FrontendApplication application) {
		LOGGER.info("setting stylesheets...");
		WBootstrapTheme theme = new WBootstrapTheme();
		theme.setVersion(WBootstrapTheme.Version.Version3);
		// load the default bootstrap3 (sub-)theme
		application.useStyleSheet(new WLink(WApplication.getRelativeResourcesUrl() + "themes/bootstrap/3/bootstrap-theme.min.css"));
		application.setTheme(theme);

		application.useStyleSheet(new WLink("style/everywidget.css"));
		application.useStyleSheet(new WLink("style/pygments.css"));
	}
}
