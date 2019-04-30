package no.siriuslabs.image.ui.container;

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
import eu.webtoolkit.jwt.WSuggestionPopup;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextArea;
import eu.webtoolkit.jwt.WValidator;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.model.Image;
import no.siriuslabs.image.services.ImageFileService;
import no.siriuslabs.image.ui.widget.AutocompleteValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Container class representing the upload page accessed from the main menu.
 */
public class UploadContainer extends AbstractAnnotationContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(UploadContainer.class);

	private WFileUpload fileUpload;
	private WPushButton uploadButton;

	private WLabel nameLabel;
	private WLineEdit nameEdit;

	private WLabel typeLabel;
	private WComboBox typeComboBox;

	private WLabel ownerLabel;
	private WLineEdit ownerEdit;
	private WSuggestionPopup ownerPopup;

	private WLabel descriptionLabel;
	private WTextArea descriptionTextArea;

	private WContainerWidget buttonContainer;
	private WPushButton cancelButton;
	private WPushButton saveButton;

	private WText messageText;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private List<Concept> image_types;

	public UploadContainer(AbstractAnnotationApplication application, WContainerWidget parent) {
		super(application, parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		WContainerWidget uploadContainer = initializeUploadContainer();
		initializeNameField();
		initializeTypeField();
		initializeOwnerField();
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

	private void initializeOwnerField() {
		ownerLabel = new WLabel("Owner:");
		ownerLabel.setMargin(new WLength(20), EnumSet.of(Side.Right));
		ownerEdit = new WLineEdit();
		ownerLabel.setBuddy(ownerEdit);

		ownerEdit.setValidator(new AutocompleteValidator(true));

		WSuggestionPopup.Options options = new WSuggestionPopup.Options();
		options.highlightBeginTag = "<span class=\"highlight\">";
		options.highlightEndTag = "</span>";
		options.listSeparator = ',';
		options.whitespace = " \\n";
		options.wordSeparators = "-., \"@\\n;";
		options.appendReplacedText = ", ";

		ownerPopup = new WSuggestionPopup(options);
		ownerPopup.forEdit(ownerEdit, EnumSet.of(WSuggestionPopup.PopupTrigger.Editing, WSuggestionPopup.PopupTrigger.DropDownIcon));

		initializeOwnerSuggestions();
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

		layout.addWidget(ownerLabel, 3, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(ownerEdit, 3, 1, 1, 2, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));

		layout.addWidget(descriptionLabel, 4, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignTextTop));
		layout.addWidget(descriptionTextArea, 4, 1, 1, 2, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignTextTop));

		layout.addWidget(messageText, 5, 1, 1, 1, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(buttonContainer, 5, 2, 1, 1, EnumSet.of(AlignmentFlag.AlignRight, AlignmentFlag.AlignBottom));

		setLayout(layout);
	}

	private void readTypeData() {
		String sessionID = getSessionID();
		image_types = getImageAnnotationAPI().getImageTypes(sessionID);

		for (Concept type : image_types) {
			typeComboBox.addItem(type.getLabel());
		}
	}

	private void initializeOwnerSuggestions() {
		ownerPopup.clearSuggestions();

		Set<String> contributors = getImageAnnotationAPI().getAvailableContributors(getSessionID());
		contributors.remove(Image.DEFAULT_CONTRIBUTOR);

		for(String s : contributors) {
			ownerPopup.addSuggestion(s);
		}
	}

	private void initializeWithDefaultValues() {
		if(typeComboBox.getCount() > 0) {
			typeComboBox.setCurrentIndex(0);
		}

		initializeOwnerSuggestions();
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
		if(WValidator.State.Invalid == ownerEdit.validate()) {
			final String message = "Only one owner is allowed";
			LOGGER.info("validation failed: " + message);
			showErrorMessage(message);
			return;
		}
		if(WValidator.State.Valid != ownerEdit.validate()) {
			final String message = "Owner must not be empty.";
			LOGGER.info("validation failed: " + message);
			showErrorMessage(message);
			return;
		}
		LOGGER.info("validation successful --> storing image");

		String tempServerPath = fileUpload.getSpoolFileName();
		String originalFilename = fileUpload.getClientFileName();

		((ImageFileService)getFileService()).storeImageFile(tempServerPath, originalFilename);

		LOGGER.info("saving data");
		
		String sessionID = getSessionID();
		GeologicalImage gimg = new GeologicalImage();
		gimg.setDescription(descriptionTextArea.getValueText());
		gimg.setLocation(originalFilename);
		gimg.setLabel(nameEdit.getValueText());
		gimg.setClassType(typeComboBox.getValueText());
		gimg.setDateSubmission(dateFormat.format(new Date()));
		gimg.setContributor(removeAutoCompleteComma(ownerEdit.getText().trim()));

		getImageAnnotationAPI().saveGeologicalImage(sessionID, gimg);

		LOGGER.info("save successful --> resetting UI");
		resetUI();
		showInfoMessage("Save successful.");
	}

	private String removeAutoCompleteComma(String label) {
		label = label.replace(',', ' ');
		label = label.trim();
		return label;
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
		ownerEdit.setText("");
		descriptionTextArea.setText("");
	}

	private void resetMessageField() {
		messageText.setText("");
		messageText.setStyleClass("");
	}
}
