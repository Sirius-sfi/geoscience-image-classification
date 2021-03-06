package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.KeyboardModifier;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WBorderLayout;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WKeyEvent;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WScrollArea;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WToolBar;
import eu.webtoolkit.jwt.WVBoxLayout;
import no.siriuslabs.image.AbstractAnnotationApplication;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GIC_URIUtils;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.ui.widget.AnnotationTableWidget;
import no.siriuslabs.image.ui.widget.CreateShapeDialog;
import no.siriuslabs.image.ui.widget.ShapeWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;
import uio.ifi.ontology.toolkit.projection.model.entities.Entity;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.ObjectProperty;
import uio.ifi.ontology.toolkit.projection.model.triples.Triple;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Container class representing the annotation page accessed from the main menu.
 */
public class AnnotationContainer extends AbstractAnnotationContainer implements PropertyChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationContainer.class);

	private static final String ZOOM_IN_BUTTON_LABEL = "+";
	private static final String ZOOM_OUT_BUTTON_LABEL = "-";
	private static final Collection<KeyboardModifier> ACTIVE_MODIFIER_KEYS = Arrays.asList(KeyboardModifier.AltModifier, KeyboardModifier.ShiftModifier);

	private final GeologicalImage image;

	private WContainerWidget shapeContainer;
	private WScrollArea shapeScrollArea;
	private WToolBar shapeToolBar;
	private ShapeWidget shapeWidget;
	private WPushButton newShapeButton;
	private WPushButton cancelShapeButton;
	private WPushButton saveAnnotationButton;

	private AnnotationTableWidget annotationTableWidget;

	private WContainerWidget topPanel;
	private WContainerWidget buttonPanel;

	private WText messageText;

	private Signal1.Listener<WKeyEvent> keyPressedlistener;
	private Signal1.Listener<WKeyEvent> keyUplistener;

	/**
	 * Constructor taking the application, parent container and the GeologicalImage that is to be annotated.
	 */
	public AnnotationContainer(AbstractAnnotationApplication application, WContainerWidget parent, GeologicalImage image) {
		super(application, parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.image = image;

		loadAnnotations();

		initializeTopPanel();
		initializeShapePanel();
		initializeAnnotationPanel();

		initializeLayout();

		initializeKeyListeners();

		LOGGER.info("{} constructor - end", getClass().getSimpleName());
	}

	private void initializeTopPanel() {
		topPanel = new WContainerWidget();
		WVBoxLayout topLayout = new WVBoxLayout();
		topPanel.setLayout(topLayout);
		topLayout.addWidget(new WText(image.getLabel() + " (" + image.getTypeLabel() + ")"));

		messageText = new WText();
		messageText.setMargin(new WLength(20), EnumSet.of(Side.Top));
		topLayout.addWidget(messageText);
	}

	private void initializeShapePanel() {
		initializeShapeButtonPanel();
		initializeShapeWidget();
		initializeShapeScrollArea();
		initializeShapeToolbar();

		WVBoxLayout shapeLayout = new WVBoxLayout();
		shapeLayout.addWidget(shapeToolBar);
		shapeLayout.addWidget(shapeScrollArea);
		shapeLayout.addWidget(buttonPanel);
		shapeContainer = new WContainerWidget();
		shapeContainer.setLayout(shapeLayout);
	}

	private void initializeShapeButtonPanel() {
		buttonPanel = new WContainerWidget();
		WHBoxLayout buttonLayout = new WHBoxLayout();

		newShapeButton = new WPushButton("New Shape");
		newShapeButton.clicked().addListener(this, (WMouseEvent arg) -> newButtonClickedAction());
		buttonLayout.addWidget(newShapeButton);

		saveAnnotationButton = new WPushButton("Save Shape...");
		saveAnnotationButton.disable();
		saveAnnotationButton.clicked().addListener(this, (WMouseEvent arg) -> saveButtonClickedAction());
		buttonLayout.addWidget(saveAnnotationButton);

		cancelShapeButton = new WPushButton("Cancel Shape");
		cancelShapeButton.disable();
		cancelShapeButton.clicked().addListener(this, (WMouseEvent arg) -> cancelButtonClickedAction());
		buttonLayout.addWidget(cancelShapeButton);

		buttonPanel.setLayout(buttonLayout);
	}

	private void initializeShapeWidget() {
		shapeWidget = new ShapeWidget(this, image);
		shapeWidget.setMinimumSize(new WLength(image.getWidth(), WLength.Unit.Pixel), new WLength(image.getHeight(), WLength.Unit.Pixel));
		shapeWidget.resize(image.getWidth(), image.getHeight()); // without this resize the widget seems to collapse to 0-size when put into the ScrollArea
		shapeWidget.addPropertyChangeListener(this);
		shapeWidget.mouseWentDown().addListener(this, arg -> mouseClickedOnImageAction(arg));

		// mouse wheel zoom interferes with moving of the scroll area, which cannot be deactivated as it seems?
//		shapeWidget.mouseWheel().addListener(this, (WMouseEvent arg) -> {
//			if(arg.getWheelDelta() < 0) {
//				decreaseImageZoomAction();
//			}
//			else if(arg.getWheelDelta() > 0) {
//				increaseImageZoomAction();
//			}
//		});
	}

	private void initializeShapeScrollArea() {
		shapeScrollArea = new WScrollArea();
		shapeScrollArea.setScrollBarPolicy(WScrollArea.ScrollBarPolicy.ScrollBarAlwaysOn);
		shapeScrollArea.setWidget(shapeWidget);
		shapeScrollArea.setMinimumSize(new WLength(image.getWidth(), WLength.Unit.Pixel), new WLength(image.getHeight(), WLength.Unit.Pixel));
		shapeScrollArea.setMaximumSize(WLength.Auto, new WLength(600, WLength.Unit.Pixel)); // TODO needed to enable vertical scrollbar - better number?
	}

	private void initializeShapeToolbar() {
		shapeToolBar = new WToolBar();

		WPushButton zoomInButton = new WPushButton(ZOOM_IN_BUTTON_LABEL);
		zoomInButton.setToolTip("Zoom in");
		zoomInButton.clicked().addListener(this, (WMouseEvent arg) -> increaseImageZoomAction());
		shapeToolBar.addButton(zoomInButton);

		WPushButton zoomOutButton = new WPushButton(ZOOM_OUT_BUTTON_LABEL);
		zoomOutButton.setToolTip("Zoom out");
		zoomOutButton.clicked().addListener(this, (WMouseEvent arg) -> decreaseImageZoomAction());
		shapeToolBar.addButton(zoomOutButton);

		shapeToolBar.addSeparator();

		WPushButton resetZoomButton = new WPushButton("Reset");
		resetZoomButton.setToolTip("Reset zoom");
		resetZoomButton.clicked().addListener(this, (WMouseEvent arg) -> resetZoomLevelAction());
		shapeToolBar.addButton(resetZoomButton);
	}

	private void initializeAnnotationPanel() {
		annotationTableWidget = new AnnotationTableWidget(this, image);
		annotationTableWidget.addPropertyChangeListener(shapeWidget);
	}

	private void initializeLayout() {
		WBorderLayout layout = new WBorderLayout();
		layout.addWidget(topPanel, WBorderLayout.Position.North);
		layout.addWidget(shapeContainer, WBorderLayout.Position.Center);
		layout.addWidget(annotationTableWidget, WBorderLayout.Position.East);

		setLayout(layout);
	}

	private void initializeKeyListeners() {
		keyPressedlistener = new KeyPressedListener();
		getApplication().globalKeyPressed().addListener(this, keyPressedlistener);

		keyUplistener = new KeyUpListener();
		getApplication().globalKeyWentUp().addListener(this, keyUplistener);
	}

	@Override
	public void cleanupOnLeave() {
		// remove key listeners to avoid them reacting in other containers or stacking up and triggering n-times
		getApplication().globalKeyPressed().removeListener(keyPressedlistener);
		getApplication().globalKeyWentUp().removeListener(keyUplistener);

		super.cleanupOnLeave();
	}

	private void mouseClickedOnImageAction(WMouseEvent arg) {
		if(isLeftMouseButton(arg) && isWidgetModeSetPoints()) {
			shapeWidget.addPointToShape(arg);
		}
		else if(isMiddleMouseButton(arg) && isShiftKeyPressed(arg)) {
			cancelButtonClickedAction();
		}
		else if(isMiddleMouseButton(arg)) {
			// while defining a shape the click means "save"...
			if(isWidgetModeSetPoints() || isWidgetModeUnsavedShape()) {
				saveButtonClickedAction();
			}
			// ...in "clean" state it means "start a new shape"
			else {
				newButtonClickedAction();
			}
		}
	}

	private void increaseImageZoomAction() {
		shapeWidget.increaseZoomLevel();
		shapeWidget.update();
	}

	private void decreaseImageZoomAction() {
		shapeWidget.decreaseZoomLevel();
		shapeWidget.update();
	}

	private void resetZoomLevelAction() {
		shapeWidget.resetZoomLevel();
		shapeWidget.update();
	}

	private void newButtonClickedAction() {
		annotationTableWidget.resetTableSelection();
		shapeWidget.setWidgetMode(ShapeWidget.AnnotationWidgetMode.SET_POINTS);
	}

	private void cancelButtonClickedAction() {
		shapeWidget.resetShape();
	}

	private void saveButtonClickedAction() {
		if(shapeWidget.hasPointsSet()) {
			if(isWidgetModeSetPoints()) {
				shapeWidget.confirmShape();
			}

			CreateShapeDialog dialog = new CreateShapeDialog(this);
			dialog.finished().addListener(this, () -> {
				if(dialog.getResult() == WDialog.DialogCode.Accepted) {
					saveShapeAndInitialTriplets(dialog);
				}

				dialog.remove();
			});

			dialog.show();
		}
	}

	private void saveShapeAndInitialTriplets(CreateShapeDialog dialog) {
		LOGGER.info("saving initial annotations");

		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		Concept selectedType = dialog.getTypeValue();
		String shapeID = imageAnnotationAPI.getNewSelectionShapeURI();

		// save differs between free text name with type selection and selection of existing object as name
		if(dialog.getSelectedNameInstance() == null) {
			imageAnnotationAPI.saveNewShapeAndObject(sessionID, image.getIri(), shapeWidget.getUnsavedShape(), shapeID, selectedType.getIri(), dialog.getNameTextValue());
		}
		else {
			final Instance selectedNameInstance = dialog.getSelectedNameInstance();
			imageAnnotationAPI.saveNewShapeAndObject(sessionID, image.getIri(), shapeWidget.getUnsavedShape(), shapeID, selectedNameInstance.getClassType(), selectedNameInstance.getIri());
		}

		shapeWidget.handleShapeSaved();

		annotationTableWidget.refreshData();

		LOGGER.info("initial save was successful");
	}

	/**
	 * Reacts to changes in child widgets.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(ShapeWidget.WIDGET_MODE_PROPERTY_NAME.equals(evt.getPropertyName())) {
			final ShapeWidget.AnnotationWidgetMode widgetMode = (ShapeWidget.AnnotationWidgetMode) evt.getNewValue();
			LOGGER.info(ShapeWidget.WIDGET_MODE_PROPERTY_NAME + " changed to {}", widgetMode);

			if(widgetMode == ShapeWidget.AnnotationWidgetMode.SET_POINTS) {
				newShapeButton.disable();
				cancelShapeButton.enable();
				saveAnnotationButton.enable();
			}
			else if(widgetMode == ShapeWidget.AnnotationWidgetMode.UNSAVED_SHAPE) {
				newShapeButton.disable();
				cancelShapeButton.enable();
				saveAnnotationButton.enable();
			}
			else if(widgetMode == ShapeWidget.AnnotationWidgetMode.SAVED_SHAPE) {
				newShapeButton.enable();
				cancelShapeButton.disable();
				saveAnnotationButton.disable();
			}
		}
	}

	private boolean isLeftMouseButton(WMouseEvent arg) {
		return WMouseEvent.Button.LeftButton.equals(arg.getButton());
	}

	private boolean isMiddleMouseButton(WMouseEvent arg) {
		return WMouseEvent.Button.MiddleButton.equals(arg.getButton());
	}

	private boolean isShiftKeyPressed(WMouseEvent arg) {
		return arg.getModifiers().contains(KeyboardModifier.ShiftModifier);
	}

	private boolean isWidgetModeSetPoints() {
		return ShapeWidget.AnnotationWidgetMode.SET_POINTS == shapeWidget.getWidgetMode();
	}

	private boolean isWidgetModeUnsavedShape() {
		return ShapeWidget.AnnotationWidgetMode.UNSAVED_SHAPE == shapeWidget.getWidgetMode();
	}

	/**
	 * Shows the given message in the message area.
	 */
	public void showErrorMessage(String message) {
		messageText.setText(message);
		messageText.setStyleClass("alert alert-danger");
	}

	/**
	 * Shows the given message in the message area.
	 */
	public void showInfoMessage(String message) {
		clearMessageField();
		messageText.setText(message);
		messageText.setStyleClass("alert alert-info");
	}

	/**
	 * Clears the message field.
	 */
	public void clearMessageField() {
		messageText.setText("");
		messageText.setStyleClass("");
	}

	/**
	 * Loads all annotations for this image from the ontology.
	 */
	public List<Triple> loadAnnotations() {
		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();
		Set<Triple> result = imageAnnotationAPI.getAllImageAnnotations(sessionID, image.getIri());

		List<Triple> annotationTriples = new ArrayList<>(result);
		annotationTriples.sort(new TripleComparator());

		return annotationTriples;
	}

	/**
	 * Comparator to sort Triples (and indirectly TripleTreeNodes) for display in the tree table.
	 * Sorting is done by:
	 * - subject label (so that all nodes of one subject are grouped together)
	 * - shape reference (so that the node carrying IS_VISUALLY_REPRESENTED and the shape reference is on top to be used as shape-node)
	 * - predicate label
	 */
	private static class TripleComparator implements Comparator<Triple> {

		private static final String SHAPE_PREFIX_FOR_SEARCH = GIC_URIUtils.SHAPE_OBJECT_PREFIX + '-';

		@Override
		public int compare(Triple o1, Triple o2) {
			final String o1Predicate = ((Entity) o1.getPredicate()).getVisualRepresentation();
			final String o2Predicate = ((Entity) o2.getPredicate()).getVisualRepresentation();

			// compare by subject
			int result = o1.getSubject().getVisualRepresentation().compareTo(o2.getSubject().getVisualRepresentation());

			if(result == 0) {
				if(o1.getPredicate().equals(o2Predicate) && isPredicateVisuallyRepresented(o1Predicate)) {
					// same predicates and predicate == IS_VISUALLY_REPRESENTED --> check for shape reference
					boolean o1IsShape = doesObjectReferToShape(o1);
					boolean o2IsShape = doesObjectReferToShape(o2);

					if(o1IsShape && !o2IsShape) {
						// o1 has shape reference, o2 not --> sort o1 up
						return -1;
					}
					if(o2IsShape && !o1IsShape) {
						// o2 has shape reference, o1 not --> sort o2 up
						return 1;
					}
					// both or none have shape reference --> fall back to default sorting below...
				}
				else if(isPredicateVisuallyRepresented(o1Predicate)) {
					// only o1 == IS_VISUALLY_REPRESENTED --> sort o1 up
					return -1;
				}
				else if(isPredicateVisuallyRepresented(o2Predicate)) {
					// only o2 == IS_VISUALLY_REPRESENTED --> sort o2 up
					return 1;
				}

				// no predicate == IS_VISUALLY_REPRESENTED or all checks equal --> sort by predicate
				return o1Predicate.compareTo(o2Predicate);
			}

			// subjects different --> return compare result
			return result;
		}

		private boolean isPredicateVisuallyRepresented(String o1Predicate) {
			return GIC_URIUtils.IS_VISUALLY_REPRESENTED.equals(o1Predicate);
		}

		private boolean doesObjectReferToShape(Triple triple) {
			return triple.getObject() instanceof ObjectProperty && ((Entity) triple.getObject()).getVisualRepresentation().startsWith(SHAPE_PREFIX_FOR_SEARCH);
		}
	}

	/**
	 * Key listener class for key pressed events to deal with "functional" keys.
	 */
	private class KeyPressedListener implements Signal1.Listener<WKeyEvent> {
		@Override
		public void trigger(WKeyEvent arg) {
			if(ZOOM_IN_BUTTON_LABEL.equals(arg.getText())) {		// + (zoom-in) button
				increaseImageZoomAction();
			}
			else if(ZOOM_OUT_BUTTON_LABEL.equals(arg.getText())) {	// - (zoom-out) button
				decreaseImageZoomAction();
			}
		}
	}

	/**
	 * Key listener class for key up events to deal with "character" keys.
	 */
	private class KeyUpListener implements Signal1.Listener<WKeyEvent> {
		@Override
		public void trigger(WKeyEvent arg) {
			if(arg.getModifiers().containsAll(ACTIVE_MODIFIER_KEYS)) {
				switch(arg.getKey()) {
					case Key_N: // New Shape button
						newButtonClickedAction();
						break;
					case Key_S:	// Save Shape button
						saveButtonClickedAction();
						break;
					case Key_C:	// Cancel Shape button
						cancelButtonClickedAction();
						break;
					case Key_R:	// Reset (zoom) button
						resetZoomLevelAction();
						break;
					case Key_A:	// Add (annotation) button
						annotationTableWidget.addAnnotationButtonClickedAction();
						break;
					case Key_E:	// Edit (annotation) button
						annotationTableWidget.editAnnotationButtonClickedAction();
						break;
					case Key_D:	// Delete (annotation) button
						annotationTableWidget.deleteAnnotationButtonClickedAction();
						break;
					default:
						// none of the combinations we are interested in - do nothing
						break;
				}
			}
		}
	}
}
