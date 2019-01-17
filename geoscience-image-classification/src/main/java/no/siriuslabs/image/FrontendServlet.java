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

	public static final String ONTOLOGY_PATH_KEY = "ontology-path";
	public static final String ONTOLOGY_FILENAME_KEY = "ontology-filename";
	public static final String ANNOTATIONS_PATH_KEY = "annotations-path";
	public static final String ANNOTATIONS_FILENAME_KEY = "annotations-filename";

	public static final String WORK_DIRECTORY = "workDirectory";
	public static final String EXTERNAL_DIRECTORY_SET = "externalDirectorySet";

	@Override
	public WApplication createApplication(WEnvironment wEnvironment) {
		LOGGER.info("creating application...");
		alterConfiguration();

		initializeEnvironment();
		initializeOntologyConnection();

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

	private void initializeEnvironment() {
		String workDirectory = System.getProperty("workDirectory");
		boolean externalDirectorySet = workDirectory != null;
		getServletContext().setAttribute(EXTERNAL_DIRECTORY_SET, externalDirectorySet);
		LOGGER.info("system property workDirectory was: {}", workDirectory);
		String workDirectoryPath = externalDirectorySet ? workDirectory : getServletContext().getRealPath("/");
		getServletContext().setAttribute(WORK_DIRECTORY, workDirectoryPath);

		FileService fileService = new FileService(getServletContext());
		getServletContext().setAttribute(FILE_SERVICE_KEY, fileService);
		// if an external directory was set, copy all images from there to the server's local image directory
		if(externalDirectorySet) {
			fileService.synchronizeImageDirectories();
			fileService.synchronizeDatafilesToInitialDataDirectory();
		}
	}

	private void initializeOntologyConnection() {
		LOGGER.info("connecting to ontology and creating services...");

		RDFoxSessionManager session = (RDFoxSessionManager) getServletContext().getAttribute(RDFoxSessionContextListener.RDFOX_SESSION);
		ImageAnnotationAPI icg = new ImageAnnotationAPI(session);

		String absoluteWebPath = (String) getServletContext().getAttribute(WORK_DIRECTORY);

		//Load default ontology
		String ontology_path = getServletContext().getInitParameter(ONTOLOGY_PATH_KEY);
		String ontology_filename = getServletContext().getInitParameter(ONTOLOGY_FILENAME_KEY);
		String annotations_path = getServletContext().getInitParameter(ANNOTATIONS_PATH_KEY);
		String annotations_filename = getServletContext().getInitParameter(ANNOTATIONS_FILENAME_KEY);

		String protocol = "file:";
		if (!absoluteWebPath.startsWith("/"))
			protocol+="/";
		
		String sessionID = protocol + absoluteWebPath + ontology_path + ontology_filename;
		LOGGER.info("sesionID is: {}", sessionID);
		
		//New session with ontology and data (available annotations)
		icg.createNewSession(
				sessionID,
				absoluteWebPath + annotations_path + annotations_filename);
		//end load default ontology
		
		getServletContext().setAttribute(IMAGE_ANNOTATION_API_KEY, icg);
		getServletContext().setAttribute(SESSION_ID_KEY, sessionID);

		LOGGER.info("ontology connected, services ready");
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
