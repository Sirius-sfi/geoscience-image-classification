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

	private static final Logger LOGGER = LoggerFactory.getLogger(TripleWidget.class);

	public static final String SAVED_PROPERTY_NAME = "tripleWidget.saved";
	public static final String CANCELLED_PROPERTY_NAME = "tripleWidget.cancelled";

	private final FrontendApplication application;

	private final PropertyChangeSupport propertyChangeSupport;

	private WLineEdit subject;
	private WSuggestionPopup subjectPopup;

	private WLineEdit predicate;
	private WSuggestionPopup predicatePopup;

	private WLineEdit object;
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

		subject = new WLineEdit();
		subject.setEmptyText("Subject");
		subjectPopup = new WSuggestionPopup(options);
		subjectPopup.forEdit(subject);
		subject.setValidator(new WValidator(true));
		fieldsLayout.addWidget(subject);

		predicate = new WLineEdit();
		predicate.setEmptyText("Predicate");
		predicatePopup = new WSuggestionPopup(options);
		predicatePopup.forEdit(predicate);
		predicate.setValidator(new WValidator(true));
		fieldsLayout.addWidget(predicate);

		object = new WLineEdit();
		object.setEmptyText("Object");
		objectPopup = new WSuggestionPopup(options);
		objectPopup.forEdit(object);
		object.setValidator(new WValidator(true));
		fieldsLayout.addWidget(object);

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
		// TODO temporary removed to clear errors
//		if(data == null) {
//			data = new TripletPlaceholder(subject.getValueText().trim(), predicate.getValueText().trim(), object.getValueText().trim());
//		}
//		else {
//			data.setSubject(subject.getValueText().trim());
//			data.setPredicate(predicate.getValueText().trim());
//			data.setObject(object.getValueText().trim());
//		}

		LOGGER.info("triggering " + SAVED_PROPERTY_NAME + " with values (S,P,O): {}, {}, {}", new Object[]{data.getSubject(), data.getPredicate(), data.getObject()});
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

		subject.setText(data.getSubject().getVisualRepresentation());
		predicate.setText(((Entity)data.getPredicate()).getVisualRepresentation());

		final Object tempObject = data.getObject();
		if(tempObject instanceof LiteralValue) {
			object.setText(((LiteralValue)tempObject).getVisualRepresentation());
		}
		else if(tempObject instanceof Entity) {
			object.setText(((Entity)tempObject).getVisualRepresentation());
		}
		else {
			object.setText(tempObject.toString());
		}

		updateSuggestions();
		LOGGER.info("triplet data set (S,P,O): {}, {}, {}", new Object[]{data.getSubject(), data.getPredicate(), data.getObject()});
	}

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
		// TODO context sensitive
	}

	/**
	 * Resets the current data object and empties the fields and suggestions.
	 */
	public void resetData() {
		data = null;
		subject.setText("");
		subjectPopup.clearSuggestions();
		predicate.setText("");
		predicatePopup.clearSuggestions();
		object.setText("");
		objectPopup.clearSuggestions();
		LOGGER.info("triplet data cleared");
	}

	/**
	 * Validates the contents of all fields.
	 */
	public List<WValidator.State> validate() {
		List<WValidator.State> results = new ArrayList<>(3);
		results.add(subject.validate());
		results.add(predicate.validate());
		results.add(object.validate());
		return results;
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
