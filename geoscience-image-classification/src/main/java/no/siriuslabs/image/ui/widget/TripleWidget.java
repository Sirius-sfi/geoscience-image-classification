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
import no.siriuslabs.image.model.triples.DataPropertyTriple;
import no.siriuslabs.image.model.triples.ObjectPropertyTriple;
import no.siriuslabs.image.model.triples.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Entity;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.LiteralValue;
import uio.ifi.ontology.toolkit.projection.model.entities.Property;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
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
		subjectPopup.forEdit(subjectField);
		subjectField.setValidator(new WValidator(true));
		fieldsLayout.addWidget(subjectField);

		predicateField = new WLineEdit();
		predicateField.setEmptyText("Predicate");
		predicatePopup = new WSuggestionPopup(options);
		predicatePopup.forEdit(predicateField);
		predicateField.setValidator(new WValidator(true));
		fieldsLayout.addWidget(predicateField);

		objectField = new WLineEdit();
		objectField.setEmptyText("Object");
		objectPopup = new WSuggestionPopup(options);
		objectPopup.forEdit(objectField);
		objectField.setValidator(new WValidator(true));
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
		// TODO make adding annotations work --> currently not working due to
			// - missing content from imageAnnotationAPI.getSubjectsResources() and imageAnnotationAPI.getObjectResources()
			// - uncertain selection of Triple class and their constructors
		// TODO make editing annotations work --> currently not working due to
			// - missing content from imageAnnotationAPI.getSubjectsResources() and imageAnnotationAPI.getObjectResources()

		Instance subject = null;
		for(Instance instance : availableSubjects) {
			if(instance.getVisualRepresentation().equals(subjectField.getValueText().trim())) {
				subject = instance;
				break;
			}
		}

		Entity predicate = null;
		for(Entity entity : availablePredicates) {
			if(entity.getVisualRepresentation().equals(predicateField.getValueText().trim())) {
				predicate = entity;
				break;
			}
		}

		// TODO how about objects that are Entities or just values?
		Instance object = null;
		for(Instance instance : availableObjects) {
			if(instance.getVisualRepresentation().equals(objectField.getValueText().trim())) {
				object = instance;
				break;
			}
		}

		if(Mode.ADD == mode) {
			// TODO temporarily disabled due to types constructors expect
//			data = new ObjectPropertyTriple(subject, predicate, object);
			// TODO how to decide which type of Triple we need to use?
//			data = new DataPropertyTriple(subject, predicate, ?);
		}
		else {
			data.setSubject(subject);
			data.setPredicate(predicate);
			data.setObject(object);
		}

		LOGGER.info("triggering " + SAVED_PROPERTY_NAME + " with values (S,P,O): {}, {}, {}", new Object[]{subject.getVisualRepresentation(), predicate.getVisualRepresentation(), object.getVisualRepresentation()});

		propertyChangeSupport.firePropertyChange(SAVED_PROPERTY_NAME, false, true);
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
	 * Sets the current data object and updates the fields.
	 */
	public void setData(Triple data) {
		this.data = data;

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
		// TODO make context sensitive
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
