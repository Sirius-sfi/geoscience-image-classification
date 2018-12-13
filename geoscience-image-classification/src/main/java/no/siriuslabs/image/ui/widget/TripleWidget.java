package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.WBoxLayout;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WSuggestionPopup;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WValidator;
import no.siriuslabs.image.model.triples.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Widget representing an annotation triple of three fields (subject, predicate, object).
 */
public class TripleWidget extends WContainerWidget {

	private static final Logger LOGGER = LoggerFactory.getLogger(TripleWidget.class);

	public static final String SAVED_PROPERTY_NAME = "tripleWidget.saved";
	public static final String CANCELLED_PROPERTY_NAME = "tripleWidget.cancelled";

	private final PropertyChangeSupport propertyChangeSupport;

	private WLineEdit part1;
	private WSuggestionPopup part1Popup;

	private WLineEdit part2;
	private WSuggestionPopup part2Popup;

	private WLineEdit part3;
	private WSuggestionPopup part3Popup;

	private Triple data;

	/**
	 * Default constructor.
	 */
	public TripleWidget() {
		LOGGER.info("{} constructor - start", getClass().getSimpleName());
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

		part1 = new WLineEdit();
		part1.setEmptyText("Subject");
		part1Popup = new WSuggestionPopup(options);
		part1Popup.forEdit(part1);
		part1.setValidator(new WValidator(true));
		fieldsLayout.addWidget(part1);

		part2 = new WLineEdit();
		part2.setEmptyText("Predicate");
		part2Popup = new WSuggestionPopup(options);
		part2Popup.forEdit(part2);
		part2.setValidator(new WValidator(true));
		fieldsLayout.addWidget(part2);

		part3 = new WLineEdit();
		part3.setEmptyText("Object");
		part3Popup = new WSuggestionPopup(options);
		part3Popup.forEdit(part3);
		part3.setValidator(new WValidator(true));
		fieldsLayout.addWidget(part3);

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
//			data = new TripletPlaceholder(part1.getValueText().trim(), part2.getValueText().trim(), part3.getValueText().trim());
//		}
//		else {
//			data.setSubject(part1.getValueText().trim());
//			data.setPredicate(part2.getValueText().trim());
//			data.setObject(part3.getValueText().trim());
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
		part1.setText(data.getSubject().getVisualRepresentation());
		// TODO temporary removed to clear errors
//		part2.setText(data.getPredicate());
//		part3.setText(data.getObject());
		LOGGER.info("triplet data set (S,P,O): {}, {}, {}", new Object[]{data.getSubject(), data.getPredicate(), data.getObject()});
	}

	/**
	 * Resets the current data object and empties the fields and suggestions.
	 */
	public void resetData() {
		data = null;
		part1.setText("");
		part1Popup.clearSuggestions();
		part2.setText("");
		part2Popup.clearSuggestions();
		part3.setText("");
		part3Popup.clearSuggestions();
		LOGGER.info("triplet data cleared");
	}

	/**
	 * Validates the contents of all fields.
	 */
	public List<WValidator.State> validate() {
		List<WValidator.State> results = new ArrayList<>(3);
		results.add(part1.validate());
		results.add(part2.validate());
		results.add(part3.validate());
		return results;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

}
