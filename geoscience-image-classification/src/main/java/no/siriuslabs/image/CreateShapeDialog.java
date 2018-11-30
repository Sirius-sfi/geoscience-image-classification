package no.siriuslabs.image;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WSuggestionPopup;
import eu.webtoolkit.jwt.WValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

/**
 * Widget for a dialog that queries type and name of a new shape.
 */
public class CreateShapeDialog extends WDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateShapeDialog.class);

	private WLabel typeLabel;
	private WLineEdit typeLineEdit;
	private WLabel nameLabel;
	private WLineEdit nameLineEdit;
	private WPushButton saveButton;

	/**
	 * Default constructor.
	 */
	public CreateShapeDialog() {
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		setWindowTitle("Save shape");
		rejectWhenEscapePressed();

		initializeTypeControls();
		initializeNameControls();

		initializeButtons();

		initializeLayout();

		setupValidation();
		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initializeTypeControls() {
		typeLabel = new WLabel("What type of feature did you mark with the shape?");

		WSuggestionPopup.Options options = new WSuggestionPopup.Options();
		options.highlightBeginTag = "<span class=\"highlight\">";
		options.highlightEndTag = "</span>";
		options.listSeparator = ',';
		options.whitespace = " \\n";
		options.wordSeparators = "-., \"@\\n;";
		options.appendReplacedText = ", ";

		typeLineEdit = new WLineEdit();
		WSuggestionPopup typePopup = new WSuggestionPopup(options);
		typePopup.forEdit(typeLineEdit);
		typeLineEdit.setValidator(new WValidator(true));
		typeLineEdit.setMaximumSize(new WLength(300), WLength.Auto);

		typeLabel.setBuddy(typeLineEdit);
	}

	private void initializeNameControls() {
		nameLabel = new WLabel("How should this feature be named?");

		nameLineEdit = new WLineEdit();
		nameLineEdit.setValidator(new WValidator(true));
		nameLineEdit.setMaximumSize(new WLength(300), WLength.Auto);

		nameLabel.setBuddy(nameLineEdit);
	}

	private void initializeButtons() {
		saveButton = new WPushButton("Save", getFooter());
		saveButton.setDefault(true);
		saveButton.clicked().addListener(this, () -> accept());
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			saveButton.disable();
		}

		WPushButton cancelButton = new WPushButton("Cancel", getFooter());
		cancelButton.clicked().addListener(this, e1 -> reject());
	}

	private boolean areFieldsValid() {
		return typeLineEdit.validate() == WValidator.State.Valid && nameLineEdit.validate() == WValidator.State.Valid;
	}

	private void initializeLayout() {
		WGridLayout layout = new WGridLayout();
		layout.addWidget(typeLabel, 1, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(typeLineEdit, 1, 1, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(nameLabel, 2, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(nameLineEdit, 2, 1, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		getContents().setLayout(layout);
	}

	private void setupValidation() {
		typeLineEdit.keyWentUp().addListener(this, () -> saveButton.setEnabled(areFieldsValid()));
		nameLineEdit.keyWentUp().addListener(this, () -> saveButton.setEnabled(areFieldsValid()));
	}

	/**
	 * Returns the value from the type field.
	 */
	public String getTypeValue() {
		// TODO return type from ontology instead
		return typeLineEdit.getValueText().trim();
	}

	/**
	 * Returns the value from the name field.
	 */
	public String getNameValue() {
		return nameLineEdit.getValueText().trim();
	}
}
