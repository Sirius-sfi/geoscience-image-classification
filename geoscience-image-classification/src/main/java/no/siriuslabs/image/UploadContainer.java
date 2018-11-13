package no.siriuslabs.image;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WFileUpload;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WProgressBar;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextArea;
import eu.webtoolkit.jwt.WValidator;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.services.FileService;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;

/**
 * Container class representing the upload page accessed from the main menu.
 */
public class UploadContainer extends WContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(UploadContainer.class);

	private final FrontendApplication application;

	private WFileUpload fileUpload;
	private WPushButton uploadButton;
	private WLabel nameLabel;
	private WLineEdit nameEdit;
	private WLabel typeLabel;
	private WComboBox typeComboBox;
	private WLabel descriptionLabel;
	private WTextArea descriptionTextArea;
	private WContainerWidget buttonContainer;
	private WPushButton cancelButton;
	private WPushButton saveButton;
	private WText messageText;

	private List<Concept> image_types;

	public UploadContainer(FrontendApplication application, WContainerWidget parent) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;

		WContainerWidget uploadContainer = initializeUploadContainer();
		initializeNameField();
		initializeTypeField();
		initializeDescriptionField();
		initializeMessageField();
		initializeButtonContainer();

		initializeLayout(uploadContainer);

		readTypeData();
		initializeWithDefaultValues();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private WContainerWidget initializeUploadContainer() {
		WContainerWidget uploadContainer = new WContainerWidget();
		WGridLayout uploadLayout = new WGridLayout();
		uploadContainer.setLayout(uploadLayout);

		fileUpload = new WFileUpload();
		fileUpload.setFileTextSize(50);
		fileUpload.setFilters("image/*");
		fileUpload.setProgressBar(new WProgressBar());
		fileUpload.setMargin(new WLength(20), EnumSet.of(Side.Right));

		fileUpload.changed().addListener(this, () -> performFileUploadChangedAction());
		fileUpload.uploaded().addListener(this, () -> performUploadCompleteAction());
		fileUpload.fileTooLarge().addListener(this, () -> performFileTooLargeAction());

		uploadButton = new WPushButton("Upload");
		uploadButton.setMargin(new WLength(10), EnumSet.of(Side.Right));
		uploadButton.disable();
		uploadButton.clicked().addListener(this, () -> performDoUploadAction());

		uploadLayout.addWidget(fileUpload, 0, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		uploadLayout.addWidget(uploadButton, 0, 1, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		return uploadContainer;
	}

	private void replaceUploadContainer() {
		fileUpload.remove();
		WContainerWidget uploadContainer = initializeUploadContainer();
		((WGridLayout)getLayout()).addWidget(uploadContainer, 0, 1, 1, 2, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignBottom));
	}

	private void initializeNameField() {
		nameLabel = new WLabel("Name:");
		nameLabel.setMargin(new WLength(20), EnumSet.of(Side.Right));
		nameEdit = new WLineEdit();
		nameLabel.setBuddy(nameEdit);

		WValidator validator = new WValidator();
		validator.setMandatory(true);

		nameEdit.setValidator(validator);
	}

	private void initializeTypeField() {
		typeLabel = new WLabel("Image Type:");
		typeLabel.setMargin(new WLength(20), EnumSet.of(Side.Right));
		typeComboBox = new WComboBox();
		typeComboBox.setNoSelectionEnabled(true);
		typeComboBox.setCurrentIndex(-1);
		typeLabel.setBuddy(typeComboBox);

		WValidator validator = new WValidator();
		validator.setMandatory(true);

		typeComboBox.setValidator(validator);
	}

	private void initializeDescriptionField() {
		descriptionLabel = new WLabel("Description:");
		descriptionLabel.setMargin(new WLength(20), EnumSet.of(Side.Right));
		descriptionTextArea = new WTextArea();
		descriptionLabel.setBuddy(descriptionTextArea);
	}

	private void initializeMessageField() {
		messageText = new WText();
		messageText.setMargin(new WLength(20), EnumSet.of(Side.Top));
	}

	private void initializeButtonContainer() {
		buttonContainer = new WContainerWidget();
		buttonContainer.setMargin(new WLength(20), EnumSet.of(Side.Top));
		WHBoxLayout buttonContainerLayout = new WHBoxLayout();
		buttonContainer.setLayout(buttonContainerLayout);

		cancelButton = new WPushButton("Cancel");
		cancelButton.setMargin(new WLength(10), EnumSet.of(Side.Right));
		cancelButton.clicked().addListener(this, () -> performCancelAction());

		saveButton = new WPushButton("Save");
		saveButton.disable();
		saveButton.clicked().addListener(this, () -> performSaveAction());

		buttonContainerLayout.addWidget(cancelButton);
		buttonContainerLayout.addWidget(saveButton);
	}

	private void initializeLayout(WContainerWidget uploadContainer) {
		WGridLayout layout = new WGridLayout();

		layout.addWidget(uploadContainer, 0, 1, 1, 2, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignBottom));

		layout.addWidget(nameLabel, 1, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(nameEdit, 1, 1, 1, 2, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		layout.addWidget(typeLabel, 2, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(typeComboBox, 2, 1, 1, 2, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		layout.addWidget(descriptionLabel, 3, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignTextTop));
		layout.addWidget(descriptionTextArea, 3, 1, 1, 2, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignTextTop));

		layout.addWidget(messageText, 4, 1, 1, 1, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(buttonContainer, 4, 2, 1, 1, EnumSet.of(AlignmentFlag.AlignRight, AlignmentFlag.AlignBottom));

		setLayout(layout);
	}

	private void readTypeData() {
		String sessionID = (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
		image_types = ((ImageAnnotationAPI)application.getServletContext().getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY)).getImageTypes(sessionID);

		for (Concept type : image_types) {
			typeComboBox.addItem(type.getLabel());
		}
	}

	private void initializeWithDefaultValues() {
		if(typeComboBox.getCount() > 0) {
			typeComboBox.setCurrentIndex(0);
		}
	}

	private void performFileUploadChangedAction() {
		LOGGER.info("file selected");
		resetMessageField();
		uploadButton.enable();
	}

	private void performDoUploadAction() {
		LOGGER.info("upload button triggered");
		if(fileUpload.canUpload()) {
			uploadButton.disable();
			LOGGER.info("fileUpload upload starting");
			fileUpload.upload();
		}
	}

	private void performFileTooLargeAction() {
		LOGGER.info("'file too large' event triggered");
		showErrorMessage("File is too large.");
		replaceUploadContainer();
	}

	private void performUploadCompleteAction() {
		LOGGER.info("'upload complete' event triggered");
		showInfoMessage("File upload finished.");
		uploadButton.disable();
		saveButton.enable();
	}

	private void performCancelAction() {
		LOGGER.info("cancel button triggered --> resetting UI");
		resetUI();
	}

	private void performSaveAction() {
		LOGGER.info("save button triggered");

		if(WValidator.State.Valid != nameEdit.validate()) {
			final String message = "Name must not be empty.";
			LOGGER.info("validation failed: " + message);
			showErrorMessage(message);
			return;
		}
		if(WValidator.State.Valid != typeComboBox.validate()) {
			final String message = "Image Type must not be empty.";
			LOGGER.info("validation failed: " + message);
			showErrorMessage(message);
			return;
		}
		LOGGER.info("validation successful --> storing image");

		String tempServerPath = fileUpload.getSpoolFileName();
		String realPath = application.getServletContext().getRealPath("./");
		String originalFilename = fileUpload.getClientFileName();

		FileService fileService = (FileService) application.getServletContext().getAttribute(FrontendServlet.FILE_SERVICE_KEY);
		fileService.storeImageFile(tempServerPath, realPath, originalFilename);

		LOGGER.info("saving data");
		// TODO save to backend

		LOGGER.info("save successful --> resetting UI");
		resetUI();
		showInfoMessage("Save successful.");
	}

	private void showErrorMessage(String message) {
		messageText.setText(message);
		messageText.setStyleClass("alert alert-danger");
	}

	private void showInfoMessage(String message) {
		resetMessageField();
		messageText.setText(message);
		messageText.setStyleClass("alert alert-info");
	}

	private void resetUI() {
		replaceUploadContainer();
		resetInputFields();
		resetMessageField();
		saveButton.disable();
		initializeWithDefaultValues();
	}

	private void resetInputFields() {
		nameEdit.setText("");
		typeComboBox.setCurrentIndex(-1);
	}

	private void resetMessageField() {
		messageText.setText("");
		messageText.setStyleClass("");
	}
}
