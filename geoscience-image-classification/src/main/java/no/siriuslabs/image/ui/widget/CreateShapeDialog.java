package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WSuggestionPopup;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WValidator;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.EntityComparator;
import no.siriuslabs.image.ui.container.AbstractAnnotationContainer;
import no.siriuslabs.image.ui.container.AnnotationContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Widget for a dialog that queries type and name of a new shape.
 */
public class CreateShapeDialog extends WDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateShapeDialog.class);

	private final AbstractAnnotationContainer parentContainer;

	private WText messageLine;

	private WLabel typeLabel;
	private WLineEdit typeLineEdit;
	private WSuggestionPopup typePopup;
	private WLabel nameLabel;
	private WLineEdit nameLineEdit;
	private WPushButton saveButton;

	private List<Concept> availableTypes;

	/**
	 * Constructor taking the parent container.
	 */
	public CreateShapeDialog(AnnotationContainer parent) {
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		parentContainer = parent;

		setWindowTitle("Save shape");
		rejectWhenEscapePressed();

		initializeMessageLine();

		initializeTypeControls();
		initializeNameControls();

		initializeButtons();

		initializeLayout();

		setupValidation();

		initializeAutoComplete();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initializeMessageLine() {
		messageLine = new WText();
		messageLine.setMaximumSize(new WLength(300), WLength.Auto);
		messageLine.setPadding(new WLength(20), EnumSet.of(Side.Bottom));
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
		typePopup = new WSuggestionPopup(options);
		typePopup.forEdit(typeLineEdit, EnumSet.of(WSuggestionPopup.PopupTrigger.Editing, WSuggestionPopup.PopupTrigger.DropDownIcon));
		typeLineEdit.setValidator(new AutocompleteValidator(true));
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
		final WValidator.State typeState = typeLineEdit.validate();
		if(WValidator.State.Invalid == typeState) {
			messageLine.setText("<h4>Only one 'type' element is allowed</h4>");
		}
		else {
			messageLine.setText("");
		}

		return typeState == WValidator.State.Valid && nameLineEdit.validate() == WValidator.State.Valid;
	}

	private void initializeLayout() {
		WGridLayout layout = new WGridLayout();
		layout.addWidget(messageLine, 1, 0, 1, 3, AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle);
		layout.addWidget(typeLabel, 2, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(typeLineEdit, 2, 1, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(nameLabel, 3, 0, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		layout.addWidget(nameLineEdit, 3, 1, EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle));
		getContents().setLayout(layout);
	}

	private void setupValidation() {
		typeLineEdit.changed().addListener(this, () -> saveButton.setEnabled(areFieldsValid()));
		nameLineEdit.keyWentUp().addListener(this, () -> saveButton.setEnabled(areFieldsValid()));
	}

	private void initializeAutoComplete() {
		String sessionID = parentContainer.getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = parentContainer.getImageAnnotationAPI();

		availableTypes = new ArrayList<>(imageAnnotationAPI.getOntologyConcepts(sessionID));
		availableTypes.sort(new EntityComparator());

		for(Concept concept : availableTypes) {
			typePopup.addSuggestion(concept.getVisualRepresentation());
		}
	}

	/**
	 * Returns the value from the type field.
	 */
	public Concept getTypeValue() {
		// TODO get rid of the ',' and do not allow multiple entries
		String typeLabel = typeLineEdit.getValueText().trim();
		typeLabel = typeLabel.replace(',', ' ');
		typeLabel = typeLabel.trim();

		for(Concept type : availableTypes) {
			if(type.getVisualRepresentation().equals(typeLabel)) {
				return type;
			}
		}

		LOGGER.warn("Selected type {} not found in concepts list.", typeLabel);
		return null;
	}

	/**
	 * Returns the value from the name field.
	 */
	public String getNameValue() {
		return nameLineEdit.getValueText().trim();
	}

}
