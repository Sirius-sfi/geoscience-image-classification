package no.siriuslabs.image.services;

import no.siriuslabs.image.FrontendServlet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static no.siriuslabs.image.FrontendServlet.ANNOTATIONS_FILENAME_KEY;
import static no.siriuslabs.image.FrontendServlet.ANNOTATIONS_PATH_KEY;
import static no.siriuslabs.image.FrontendServlet.ONTOLOGY_FILENAME_KEY;
import static no.siriuslabs.image.FrontendServlet.ONTOLOGY_ANNOTATIONS_FILENAME_KEY;
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

	protected ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * Synchronizes the ontology and annotations files from the server deployment to the data directory (if present) should the be missing there.
	 */
	public void synchronizeDatafilesToInitialDataDirectory() {
		if(Boolean.TRUE.equals(servletContext.getAttribute(FrontendServlet.EXTERNAL_DIRECTORY_SET))) {
			synchronizeOntologies();
			synchronizeAnnotations();
		}
	}

	private void synchronizeOntologies() {
		LOGGER.info("synchronizing ontology files to data directory if initial start-up");
		synchronizeToDataDirectory(ONTOLOGY_PATH_KEY, ONTOLOGY_FILENAME_KEY, true);
		LOGGER.info("synchronizing annotations ontology files to data directory if initial start-up");
		synchronizeToDataDirectory(ONTOLOGY_PATH_KEY, ONTOLOGY_ANNOTATIONS_FILENAME_KEY, true);
	}

	private void synchronizeAnnotations() {
		LOGGER.info("synchronizing annotation files to data directory if initial start-up");
		synchronizeToDataDirectory(ANNOTATIONS_PATH_KEY, ANNOTATIONS_FILENAME_KEY, false);
	}

	/**
	 * Tries to create a directory in the given path. If the directory already exists, the method quietly does nothing.
	 * I case of an error on creating the directory, an (unchecked) exception is thrown.
	 */
	public void createDirectory(String path) {
		File imageDir = new File(path);
		if(!imageDir.exists()) {
			LOGGER.info("directory {} does not exist - creating", path);
			boolean success = imageDir.mkdir();
			LOGGER.info("directory creation at {} {}", path, success ? "successful" : "failed");
			if(!success) {
				throw new UncheckedIOException(new IOException("Creation of image directory failed."));
			}
		}
	}

	/**
	 * Copies the given sourceFile to destinationFile.
	 * Returns true if the operation was successful.
	 */
	public boolean copyFile(File sourceFile, File destinationFile) {
		try {
			FileUtils.copyFile(sourceFile, destinationFile);
			LOGGER.info("synchronizing file: {} to {}", sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath());
			return true;
		}
		catch(IOException e) {
			LOGGER.error("error while trying to copy {} to {}", new Object[]{sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath(), e});
			return false;
		}
	}

	private void synchronizeToDataDirectory(String resourcePathKey, String resourceFilenameKey, boolean forceUpdateFromServer) {
		String directoryPath = servletContext.getInitParameter(resourcePathKey);
		String filename = servletContext.getInitParameter(resourceFilenameKey);

		String externalDirectoryPath = getBasePath() + directoryPath;
		File externalDirectory = new File(externalDirectoryPath);
		if(!externalDirectory.exists()) {
			createDirectory(externalDirectoryPath);
		}

		String serverLocalDataFilePath = getServerBasePath() + directoryPath + filename;
		File serverLocalDataFile = new File(serverLocalDataFilePath);

		String absoluteExternalPath = externalDirectoryPath + filename;
		File externalFile = new File(absoluteExternalPath);

		// copy if not present; if existing preserve annotations but always bring new version of ontologies
		if (!externalFile.exists() || forceUpdateFromServer) {
			copyFile(serverLocalDataFile, externalFile);
		}
	}

	/**
	 * Returns the application's working directory (which depending on configuration might be set to the "real" path).
	 */
	public String getBasePath() {
		return (String) servletContext.getAttribute(FrontendServlet.WORK_DIRECTORY);
	}

	/**
	 * Returns the application's "real" path.
	 */
	public String getServerBasePath() {
		return servletContext.getRealPath("./");
	}

	/**
	 * Dumps all contents of the external data directory into a ZIP archive.
	 */
	public String dumpDataDirectoryToZipArchive() {
		String zipFilename = "data.zip";

		deleteFile(zipFilename);
		createZipFileFromDirectory(getBasePath(), zipFilename);

		return zipFilename;
	}

	/**
	 * Deletes the given file if present.
	 */
	public void deleteFile(String filename) {
		LOGGER.info("checking for existing file {}", filename);
		File fileToDelete = new File(filename);

		if(fileToDelete.exists()) {
			boolean result = fileToDelete.delete();
			LOGGER.info("deleting file {} - {}", filename, result ? "successful" : "failed");
		}
	}

	/**
	 * Creates a ZIP archive with the given zipFilename from the contents of the given directoryPath and its subdirectories.
	 */
	public void createZipFileFromDirectory(String directoryPath, String zipFilename) {
		try {
			LOGGER.info("starting compression of directory {} into archive {}", directoryPath, zipFilename);

			FileOutputStream fos = new FileOutputStream(zipFilename);
			ZipOutputStream zos = new ZipOutputStream(fos);

			File dir = new File(directoryPath);

			String archiveRootPath = directoryPath.endsWith("/") ? directoryPath.substring(0, directoryPath.length()-1) : directoryPath;

			compressDirectory(dir, zos, archiveRootPath);

			zos.close();
			fos.close();

			LOGGER.info("archive {} complete", zipFilename);
		}
		catch(IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void compressDirectory(File parentDirectory, ZipOutputStream zos, String archiveRootPath) throws IOException {
		LOGGER.info("compressing files in directory {}", parentDirectory.getAbsolutePath());

		File[] files = parentDirectory.listFiles();
		if(files != null) {
			byte[] buffer = new byte[1024];

			for(final File currentFile : files) {
				if(currentFile.isDirectory()) {
					LOGGER.info("{} is a directory - will compress contents", currentFile.getAbsolutePath());
					compressDirectory(currentFile, zos, archiveRootPath);
				}
				else {
					LOGGER.info("{} is a file - compressing into archive", currentFile.getAbsolutePath());

					try(FileInputStream fis = new FileInputStream(currentFile)) {
						String destinationPath = buildInArchivePath(currentFile, parentDirectory, archiveRootPath);

						zos.putNextEntry(new ZipEntry(destinationPath));
						copyFileToArchive(fis, zos, buffer);
						zos.closeEntry();
					}
					LOGGER.info("file {} done", currentFile.getAbsolutePath());
				}
			}
		}
	}

	private String buildInArchivePath(File currentFile, File directory, String archiveRootPath) {
		// cut archiveRootPath from parent directory's path to get the relative path inside the archive
		String inArchivePath = directory.getAbsolutePath().replace(archiveRootPath, "");
		// add filename to get the file's destination path inside the archive
		inArchivePath = inArchivePath + '/' + currentFile.getName();
		return inArchivePath;
	}

	private void copyFileToArchive(FileInputStream fis, ZipOutputStream zos, byte[] buffer) throws IOException {
		int length;
		while((length = fis.read(buffer)) > 0) {
			zos.write(buffer, 0, length);
		}
	}

}
