package no.siriuslabs.image.services;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Service class providing file-based functionality.
 */
public class FileService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

	public static final String IMAGE_DIRECTORY_NAME = "images";

	/**
	 * Stores an image file permanently from a temporary location.
	 * @param tempServerPath  Path to the temporary uploaded file
	 * @param basePath  Basic path from which the application operates. The file will be stored in a directory under this path defined by IMAGE_DIRECTORY_NAME
	 * @param originalFilename  Original filename of the file uploaded by the client
	 */
	public void storeImageFile(String tempServerPath, String basePath, String originalFilename) {
		try {
			final String imagePath = basePath + IMAGE_DIRECTORY_NAME;
			File imageDir = new File(imagePath);
			if(!imageDir.exists()) {
				LOGGER.info("image directory {} does not exist - creating", imagePath);
				boolean success = imageDir.mkdir();
				if(!success) {
					LOGGER.info("image directory creation at {} {}", imagePath, success ? "successful" : "failed");
					throw new UncheckedIOException(new IOException("Creation of image directory failed."));
				}
			}

			File destFile = new File(basePath + "images/" + originalFilename);

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

}
