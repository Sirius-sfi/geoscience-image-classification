package no.siriuslabs.image.services;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

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
			final String imagePath = getImagePathInEnvironment();
			File imageDir = new File(imagePath);
			if(!imageDir.exists()) {
				LOGGER.info("image directory {} does not exist - creating", imagePath);
				boolean success = imageDir.mkdir();
				if(!success) {
					LOGGER.info("image directory creation at {} {}", imagePath, success ? "successful" : "failed");
					throw new UncheckedIOException(new IOException("Creation of image directory failed."));
				}
			}

			File destFile = new File(getAbsolutePathForFile(originalFilename));

			if(destFile.exists()) {
				LOGGER.warn("image named {} exists - overwriting", destFile);
			}

			LOGGER.info("copying image from {} to {}", tempServerPath, destFile);
			File tempfile = new File(tempServerPath);
			FileUtils.copyFile(tempfile, destFile);
		}
		catch(IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Returns the application's "real" path.
	 */
	public String getBasePath() {
		return servletContext.getRealPath("./");
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
		return getImagePathInEnvironment() + "/" + filename;
	}

	/**
	 * Returns the relative image path for the given filename (image directory + filename).
	 */
	public String getRelativeImagePathForFile(String filename) {
		return getImageDirectoryName() + "/" + filename;
	}

}
