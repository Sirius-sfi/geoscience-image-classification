package no.siriuslabs.image.services;

import no.siriuslabs.image.FrontendServlet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static no.siriuslabs.image.FrontendServlet.ANNOTATIONS_FILENAME_KEY;
import static no.siriuslabs.image.FrontendServlet.ANNOTATIONS_PATH_KEY;
import static no.siriuslabs.image.FrontendServlet.ONTOLOGY_FILENAME_KEY;
import static no.siriuslabs.image.FrontendServlet.ONTOLOGY_PATH_KEY;

/**
 * Service class providing file-based functionality.
 */
public class FileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

	public static final String IMAGE_DIRECTORY_NAME = "images";

	private final ServletContext servletContext;

	/**
	 * Constructor taking the servlet context for access to the environment.
	 */
	public FileService(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Stores an image file permanently from a temporary location.
	 * @param tempServerPath  Path to the temporary uploaded file
	 * @param originalFilename  Original filename of the file uploaded by the client
	 */
	public void storeImageFile(String tempServerPath, String originalFilename) {
		try {
			String imagePath = getImagePathInEnvironment();
			createDirectory(imagePath);

			File destFile = new File(getAbsolutePathForFile(originalFilename));

			if(destFile.exists()) {
				LOGGER.warn("image named {} exists - overwriting", destFile);
			}

			LOGGER.info("copying image from {} to {}", tempServerPath, destFile);
			File tempfile = new File(tempServerPath);
			FileUtils.copyFile(tempfile, destFile);

			// if an external directory was set, also save the image to the server's image directory
			if(Boolean.TRUE.equals(servletContext.getAttribute(FrontendServlet.EXTERNAL_DIRECTORY_SET))) {
				String serverLocalImagePath = getAbsoluteImageDirectoryPath();
				createDirectory(serverLocalImagePath);
				File serverFile = new File(getAbsoluteImageDirectoryPath() + '/' + originalFilename);
				FileUtils.copyFile(tempfile, serverFile);
			}
		}
		catch(IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Synchronizes all images in the external image directory to the server's local image directory.
	 */
	public void synchronizeImageDirectories() {
		if(Boolean.TRUE.equals(servletContext.getAttribute(FrontendServlet.EXTERNAL_DIRECTORY_SET))) {
			String serverLocalImagePath = getAbsoluteImageDirectoryPath();
			createDirectory(serverLocalImagePath);

			String imagePath = getImagePathInEnvironment();
			File imageDirectory = new File(imagePath);
			if(!imageDirectory.exists()) {
				LOGGER.info("image directory " + imageDirectory.getAbsolutePath() + " does not exist - nothing to synchronize");
				return;
			}

			for(File file : imageDirectory.listFiles()) {
				File serverFile = new File(getAbsoluteImageDirectoryPath() + '/' + file.getName());
				copyFile(file, serverFile);
			}
		}
	}

	/**
	 * Synchronizes the ontology and annotations files from the server deployment to the data directory (if present) should the be missing there.
	 */
	public void synchronizeDatafilesToInitialDataDirectory() {
		if(Boolean.TRUE.equals(servletContext.getAttribute(FrontendServlet.EXTERNAL_DIRECTORY_SET))) {
			synchronizeOntology();
			synchronizeAnnotations();
		}
	}

	private void synchronizeOntology() {
		LOGGER.info("synchronizing ontology files to data directory if initial start-up");
		synchronizeToDataDirectory(ONTOLOGY_PATH_KEY, ONTOLOGY_FILENAME_KEY);
	}

	private void synchronizeAnnotations() {
		LOGGER.info("synchronizing annotation files to data directory if initial start-up");
		synchronizeToDataDirectory(ANNOTATIONS_PATH_KEY, ANNOTATIONS_FILENAME_KEY);
	}

	private void createDirectory(String path) {
		File imageDir = new File(path);
		if(!imageDir.exists()) {
			LOGGER.info("directory {} does not exist - creating", path);
			boolean success = imageDir.mkdir();
			if(!success) {
				LOGGER.info("directory creation at {} {}", path, success ? "successful" : "failed");
				throw new UncheckedIOException(new IOException("Creation of image directory failed."));
			}
		}
	}

	private boolean copyFile(File sourceFile, File serverFile) {
		try {
			FileUtils.copyFile(sourceFile, serverFile);
			LOGGER.info("synchronizing file: {} to {}", sourceFile.getAbsolutePath(), serverFile.getAbsolutePath());
			return true;
		}
		catch(IOException e) {
			LOGGER.error("error while trying to copy {} to {}", new Object[]{sourceFile.getAbsolutePath(), serverFile.getAbsolutePath(), e});
			return false;
		}
	}

	private void synchronizeToDataDirectory(String resourcePathKey, String resourceFilenameKey) {
		String directoryPath = servletContext.getInitParameter(resourcePathKey);
		String filename = servletContext.getInitParameter(resourceFilenameKey);

		String externalDirectoryPath = getBasePath() + directoryPath;
		File externalDirectory = new File(externalDirectoryPath);
		if(!externalDirectory.exists()) {
			createDirectory(externalDirectoryPath);

			String serverLocalDataFilePath = getServerBasePath() + directoryPath + filename;
			File serverLocalDataFile = new File(serverLocalDataFilePath);
			String absoluteExternalPath = externalDirectoryPath + filename;
			File externalFile = new File(absoluteExternalPath);

			copyFile(serverLocalDataFile, externalFile);
		}
	}

	/**
	 * Returns the application's "real" path.
	 */
	public String getBasePath() {
		return (String) servletContext.getAttribute(FrontendServlet.WORK_DIRECTORY);
	}

	/**
	 * Returns the application's image path (real + image path).
	 */
	public String getImagePathInEnvironment() {
		return getBasePath() + IMAGE_DIRECTORY_NAME;
	}

	/**
	 * Returns the name of the image directory.
	 */
	public String getImageDirectoryName() {
		return IMAGE_DIRECTORY_NAME;
	}

	/**
	 * Returns the absolute path for the given filename.
	 */
	public String getAbsolutePathForFile(String filename) {
		return getImagePathInEnvironment() + '/' + filename;
	}

	/**
	 * Returns the absolute path of the image directory.
	 */
	public String getAbsoluteImageDirectoryPath() {
		return getServerBasePath() + getImageDirectoryName();
	}

	private String getServerBasePath() {
		return servletContext.getRealPath("./");
	}

	/**
	 * Returns the relative image path for the given filename (image directory + filename).
	 */
	public String getRelativeImagePathForFile(String filename) {
		return getImageDirectoryName() + '/' + filename;
	}

}
