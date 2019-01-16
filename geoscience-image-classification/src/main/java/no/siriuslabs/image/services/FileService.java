package no.siriuslabs.image.services;

import no.siriuslabs.image.FrontendServlet;
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
	 * Synchronizes all images in the external image directory to the server's local image directory.
	 */
	public void synchronizeImageDirectories() {
		if(Boolean.TRUE.equals(servletContext.getAttribute(FrontendServlet.EXTERNAL_DIRECTORY_SET))) {
			String serverLocalImagePath = getAbsoluteImageDirectoryPath();
			createDirectory(serverLocalImagePath);

			String imagePath = getImagePathInEnvironment();
			File imageDirectory = new File(imagePath);
			for(File file : imageDirectory.listFiles()) {
				File serverFile = new File(getAbsoluteImageDirectoryPath() + '/' + file.getName());
				try {
					FileUtils.copyFile(file, serverFile);
					LOGGER.info("synchronizing file: " + file.getAbsolutePath() + " to " + serverFile.getAbsolutePath());
				}
				catch(IOException e) {
					LOGGER.error("error while trying to copy {} to {}", new Object[]{file.getAbsolutePath(), serverFile.getAbsolutePath(), e});
				}
			}
		}
	}

	public String getAbsoluteImageDirectoryPath() {
		return servletContext.getRealPath("./") + getImageDirectoryName();
	}

	/**
	 * Returns the relative image path for the given filename (image directory + filename).
	 */
	public String getRelativeImagePathForFile(String filename) {
		return getImageDirectoryName() + '/' + filename;
	}

}
