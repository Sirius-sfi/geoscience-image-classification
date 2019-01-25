package no.siriuslabs.image.ui.widget;

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
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.FrontendServlet;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;

import java.util.EnumSet;
import java.util.TreeSet;

/**
 * Widget for a dialog that queries type and name of a new shape.
 */
public class CreateShapeDialog extends WDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateShapeDialog.class);

	private final AbstractAnnotationApplication application;

	private WLabel typeLabel;
	private WLineEdit typeLineEdit;
	private WSuggestionPopup typePopup;
	private WLabel nameLabel;
	private WLineEdit nameLineEdit;
	private WPushButton saveButton;

	private TreeSet<Concept> availableTypes;

	/**
	 * Default constructor.
	 */
	public CreateShapeDialog(AbstractAnnotationApplication application) {
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;

		setWindowTitle("Save shape");
		rejectWhenEscapePressed();

		initializeTypeControls();
		initializeNameControls();

		initializeButtons();

		initializeLayout();

		setupValidation();

		initializeAutoComplete();

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
		typePopup = new WSuggestionPopup(options);
		typePopup.forEdit(typeLineEdit, EnumSet.of(WSuggestionPopup.PopupTrigger.Editing, WSuggestionPopup.PopupTrigger.DropDownIcon));
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

	private void initializeAutoComplete() {
		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		// TODO we want only types here...
		availableTypes = imageAnnotationAPI.getOntologyConcepts(sessionID);

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

		LOGGER.warn("Selected type " + typeLabel + " not found in conepts list.");
		return null;
	}

	/**
	 * Returns the value from the name field.
	 */
	public String getNameValue() {
		return nameLineEdit.getValueText().trim();
	}

	private String getSessionID() {
		return (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
	}

	private ImageAnnotationAPI getImageAnnotationAPI() {
		return (ImageAnnotationAPI) application.getServletContext().getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY);
	}
}
