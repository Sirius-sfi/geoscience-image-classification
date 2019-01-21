package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WSuggestionPopup;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WValidator;
import no.siriuslabs.image.FrontendApplication;
import no.siriuslabs.image.FrontendServlet;
import no.siriuslabs.image.api.ImageAnnotationAPI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.DataProperty;
import uio.ifi.ontology.toolkit.projection.model.entities.Entity;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.LiteralValue;
import uio.ifi.ontology.toolkit.projection.model.entities.ObjectProperty;
import uio.ifi.ontology.toolkit.projection.model.entities.Property;
import uio.ifi.ontology.toolkit.projection.model.triples.DataPropertyTriple;
import uio.ifi.ontology.toolkit.projection.model.triples.ObjectPropertyTriple;
import uio.ifi.ontology.toolkit.projection.model.triples.Triple;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Widget representing an annotation triple of three fields (subject, predicate, object).
 */
public class TripleWidget extends WContainerWidget {

	public enum Mode {
		ADD, EDIT;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TripleWidget.class);

	public static final String SAVED_PROPERTY_NAME = "tripleWidget.saved";
	public static final String CANCELLED_PROPERTY_NAME = "tripleWidget.cancelled";

	private final FrontendApplication application;

	private final PropertyChangeSupport propertyChangeSupport;

	private Mode mode;

	private WLineEdit subjectField;
	private WSuggestionPopup subjectPopup;

	private WLineEdit predicateField;
	private WSuggestionPopup predicatePopup;

	private WLineEdit objectField;
	private WSuggestionPopup objectPopup;

	private Triple data;
	private Triple originalData;

	private TreeSet<Instance> availableSubjects;
	private TreeSet<Property> availablePredicates;
	private TreeSet<Instance> availableObjects;

	/**
	 * Default constructor.
	 */
	public TripleWidget(FrontendApplication application) {
		LOGGER.info("{} constructor - start", getClass().getSimpleName());
		this.application = application;

		initialize();

		propertyChangeSupport = new PropertyChangeSupport(this);
		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initialize() {
		WVBoxLayout layout = new WVBoxLayout();
		setLayout(layout);

		WHBoxLayout fieldsLayout = new WHBoxLayout();

		WSuggestionPopup.Options options = new WSuggestionPopup.Options();
		options.highlightBeginTag = "<span class=\"highlight\">";
		options.highlightEndTag = "</span>";
		options.listSeparator = ',';
		options.whitespace = " \\n";
		options.wordSeparators = "-., \"@\\n;";
		options.appendReplacedText = ", ";

		subjectField = new WLineEdit();
		subjectField.setEmptyText("Subject");
		subjectPopup = new WSuggestionPopup(options);
		subjectPopup.forEdit(subjectField, EnumSet.of(WSuggestionPopup.PopupTrigger.Editing, WSuggestionPopup.PopupTrigger.DropDownIcon));
		subjectField.setValidator(new WValidator(true));
		fieldsLayout.addWidget(subjectField);

		predicateField = new WLineEdit();
		predicateField.setEmptyText("Predicate");
		predicatePopup = new WSuggestionPopup(options);
		predicatePopup.forEdit(predicateField, EnumSet.of(WSuggestionPopup.PopupTrigger.Editing, WSuggestionPopup.PopupTrigger.DropDownIcon));
		predicateField.setValidator(new WValidator(true));
		fieldsLayout.addWidget(predicateField);

		objectField = new WLineEdit();
		objectField.setEmptyText("Object");
		objectPopup = new WSuggestionPopup(options);
		objectPopup.forEdit(objectField, EnumSet.of(WSuggestionPopup.PopupTrigger.Editing, WSuggestionPopup.PopupTrigger.DropDownIcon));
		objectField.setValidator(new WValidator(true));
		objectField.focussed().addListener(this, () -> updateObjectSuggestions()); // TODO possible performance issue?
		fieldsLayout.addWidget(objectField);

		layout.addLayout(fieldsLayout);


		WHBoxLayout buttonsLayout = new WHBoxLayout();
		buttonsLayout.setDirection(WBoxLayout.Direction.RightToLeft);

		WPushButton cancelButton = new WPushButton("Cancel");
		cancelButton.clicked().addListener(this, arg -> cancelButtonClickedAction());
		buttonsLayout.addWidget(cancelButton);

		WPushButton saveButton = new WPushButton("Save");
		saveButton.clicked().addListener(this, arg -> saveButtonClickedAction());
		buttonsLayout.addWidget(saveButton);

		layout.addLayout(buttonsLayout);
	}

	private void saveButtonClickedAction() {
		// find the data representations for the texts/labels entered or selected

		Instance subject = getSubjectInstanceFromLabel();
		Property predicate = getPredicateInstanceFromLabel();

		Instance objectInstance = null;
		LiteralValue objectValue = null;
		if(predicate.isObjectProperty()) {
			for(Instance instance : availableObjects) {
				String objectLabel = objectField.getValueText().trim();
				objectLabel = removeAutoCompleteComma(objectLabel);
				if(instance.getVisualRepresentation().equals(objectLabel)) {
					objectInstance = instance;
					break;
				}
			}
		}
		else {			
			String objectLabel = objectField.getValueText().trim();
			objectLabel = removeAutoCompleteComma(objectLabel);
			objectValue = new LiteralValue(objectLabel);
		}


		if(Mode.ADD == mode) {
			if(predicate.isObjectProperty()) {
				data = new ObjectPropertyTriple(subject, (ObjectProperty) predicate, objectInstance);
			}
			else {
				data = new DataPropertyTriple(subject, (DataProperty) predicate, objectValue);
			}
		}
		else {
			data.setSubject(subject);
			data.setPredicate(predicate);
			data.setObject(predicate.isObjectProperty() ? objectInstance : objectValue);
		}

		LOGGER.info("triggering " + SAVED_PROPERTY_NAME + " with values (S,P,O): {}, {}, {}",
				new Object[]{subject.getVisualRepresentation(), predicate.getVisualRepresentation(), predicate.isObjectProperty() ? objectInstance.getVisualRepresentation() : objectValue.getValue()});

		propertyChangeSupport.firePropertyChange(SAVED_PROPERTY_NAME, false, true);
	}

	private Instance getSubjectInstanceFromLabel() {
		String subjectLabel = "";
		for(Instance instance : availableSubjects) {
			subjectLabel = subjectField.getValueText().trim();
			subjectLabel = removeAutoCompleteComma(subjectLabel);
			if(instance.getVisualRepresentation().equals(subjectLabel)) {
				return instance;
			}
		}
		LOGGER.warn("No subject instance found for field-value {}", subjectLabel);
		return null; // this should never happen for subjects
	}

	private Property getPredicateInstanceFromLabel() {
		String predicateLabel = "";
		for(Property property : availablePredicates) {
			predicateLabel = predicateField.getValueText().trim();
			predicateLabel = removeAutoCompleteComma(predicateLabel);
			if(property.getVisualRepresentation().equals(predicateLabel)) {
				return property;
			}
		}
		LOGGER.warn("No predicate instance found for field-value {}", predicateLabel);
		return null; // this should never happen for predicates
	}

	private String removeAutoCompleteComma(String label) {
		label = label.replace(',', ' ');
		label = label.trim();
		return label;
	}

	private void cancelButtonClickedAction() {
		resetData();
		LOGGER.info("triggering " + CANCELLED_PROPERTY_NAME);
		propertyChangeSupport.firePropertyChange(CANCELLED_PROPERTY_NAME, false, true);
	}

	/**
	 * Returns the current data object.
	 */
	public Triple getData() {
		return data;
	}

	/**
	 * Returns the original, unchanged version of the current data object.
	 */
	public Triple getOriginalData() {
		return originalData;
	}

	public void setSubject(Instance subject) {
		subjectField.setText(subject.getVisualRepresentation());
		updateSuggestions();
		updatePredicateSuggestions();
	}

	private void updatePredicateSuggestions() {
		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		final Instance subjectInstance = getSubjectInstanceFromLabel();
		availablePredicates = imageAnnotationAPI.getAllowedPredicatesForSubject(sessionID, subjectInstance.getIri());

		predicatePopup.clearSuggestions();
		for(Property pred : availablePredicates) {
			predicatePopup.addSuggestion(pred.getVisualRepresentation());
		}
	}

	private void updateObjectSuggestions() {
		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		final Instance subjectInstance = getSubjectInstanceFromLabel();
		final Property predicate = getPredicateInstanceFromLabel();
		availableObjects = imageAnnotationAPI.getAllowedObjectValuesForSubjectPredicate(sessionID, subjectInstance.getIri(), predicate.getIri());

		objectPopup.clearSuggestions();
		for(Instance obj : availableObjects) {
			objectPopup.addSuggestion(obj.getVisualRepresentation());
		}
	}
	
	//TODO updateObjectSuggestions for literal values? e.g. company names, true or false, etc.
	

	/**
	 * Sets the current data object and updates the fields.
	 */
	public void setData(Triple data) {
		this.data = data;

		originalData = data instanceof ObjectPropertyTriple
				? new ObjectPropertyTriple(data.getSubject(), ((ObjectPropertyTriple) data).getPredicate(), ((ObjectPropertyTriple) data).getObject())
				: new DataPropertyTriple(data.getSubject(), ((DataPropertyTriple) data).getPredicate(), ((DataPropertyTriple) data).getObject());

		subjectField.setText(data.getSubject().getVisualRepresentation());
		predicateField.setText(((Entity)data.getPredicate()).getVisualRepresentation());

		final Object tempObject = data.getObject();
		if(tempObject instanceof LiteralValue) {
			objectField.setText(((LiteralValue)tempObject).getVisualRepresentation());
		}
		else if(tempObject instanceof Entity) {
			objectField.setText(((Entity)tempObject).getVisualRepresentation());
		}
		else {
			objectField.setText(tempObject.toString());
		}

		updateSuggestions();
		LOGGER.info("triple data set (S,P,O): {}, {}, {}", new Object[]{data.getSubject(), data.getPredicate(), data.getObject()});
	}

	/**
	 * Updates the contents of the auto-complete suggestions for the SPO fields.
	 */
	public void updateSuggestions() {
		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		availableSubjects = imageAnnotationAPI.getSubjectsResources(sessionID);
		for(Instance sub : availableSubjects) {
			subjectPopup.addSuggestion(sub.getVisualRepresentation());
		}

		availablePredicates = imageAnnotationAPI.getPredicates(sessionID);
		for(Property pred : availablePredicates) {
			predicatePopup.addSuggestion(pred.getVisualRepresentation());
		}

		availableObjects = imageAnnotationAPI.getObjectResources(sessionID);
		for(Instance obj : availableObjects) {
			objectPopup.addSuggestion(obj.getVisualRepresentation());
		}
	}

	/**
	 * Resets the current data object and empties the fields and suggestions.
	 */
	public void resetData() {
		data = null;
		subjectField.setText("");
		subjectPopup.clearSuggestions();
		predicateField.setText("");
		predicatePopup.clearSuggestions();
		objectField.setText("");
		objectPopup.clearSuggestions();
		LOGGER.info("triplet data cleared");
	}

	/**
	 * Validates the contents of all fields.
	 */
	public List<WValidator.State> validate() {
		List<WValidator.State> results = new ArrayList<>(3);
		results.add(subjectField.validate());
		results.add(predicateField.validate());
		results.add(objectField.validate());
		return results;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	private String getSessionID() {
		return (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
	}

	private ImageAnnotationAPI getImageAnnotationAPI() {
		return (ImageAnnotationAPI) application.getServletContext().getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

}
