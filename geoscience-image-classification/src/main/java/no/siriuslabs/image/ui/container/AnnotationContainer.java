package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.Icon;
import eu.webtoolkit.jwt.KeyboardModifier;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.WBorderLayout;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WValidator;
import no.siriuslabs.image.FrontendApplication;
import no.siriuslabs.image.FrontendServlet;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.model.triples.Triple;
import no.siriuslabs.image.ui.widget.CreateShapeDialog;
import no.siriuslabs.image.ui.widget.ShapeWidget;
import no.siriuslabs.image.ui.widget.TripleTableModel;
import no.siriuslabs.image.ui.widget.TripleWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Container class representing the annotation page accessed from the main menu.
 */
public class AnnotationContainer extends WContainerWidget implements PropertyChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationContainer.class);

	private final FrontendApplication application;
	private final GeologicalImage image;

	private WContainerWidget shapeContainer;
	private ShapeWidget shapeWidget;
	private WPushButton newShapeButton;
	private WPushButton cancelShapeButton;
	private WPushButton saveAnnotationButton;

	private WVBoxLayout annotationLayout;
	private WTableView annotationsTable; // TODO replace with a tree-table to get a visual relation to the shape/feature?
	private TripleWidget annotationEditorWidget;

	private WContainerWidget topPanel;
	private WContainerWidget annotationPanel;
	private WContainerWidget buttonPanel;
	private WPushButton addAnnotationButton;
	private WPushButton editAnnotationButton;
	private WPushButton deleteAnnotationButton;

	private WText messageText;

	private List<Triple> annotationTriples;

	/**
	 * Constructor taking the application, parent container and the GeologicalImage that is to be annotated.
	 */
	public AnnotationContainer(FrontendApplication application, WContainerWidget parent, GeologicalImage image) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;
		this.image = image;

		loadAnnotations();

		initializeTopPanel();
		initializeShapeWidget();
		initializeAnnotationPanel();

		initializeLayout();

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

	private void initializeShapeWidget() {
		initializeShapeButtonPanel();

		shapeWidget = new ShapeWidget(application, image);
		shapeWidget.setMinimumSize(new WLength(800, WLength.Unit.Pixel), new WLength(600, WLength.Unit.Pixel));
		shapeWidget.addPropertyChangeListener(this);
		shapeWidget.mouseWentDown().addListener(this, arg -> mouseClickedOnImageAction(arg));

		shapeContainer = new WContainerWidget();
		WVBoxLayout shapeLayout = new WVBoxLayout();
		shapeLayout.addWidget(shapeWidget);
		shapeLayout.addWidget(buttonPanel);
		shapeContainer.setLayout(shapeLayout);
	}

	private void initializeShapeButtonPanel() {
		buttonPanel = new WContainerWidget();
		WHBoxLayout buttonLayout = new WHBoxLayout();

		newShapeButton = new WPushButton("New Shape");
		newShapeButton.clicked().addListener(this, arg -> newButtonClickedAction());
		buttonLayout.addWidget(newShapeButton);

		saveAnnotationButton = new WPushButton("Save Shape...");
		saveAnnotationButton.disable();
		saveAnnotationButton.clicked().addListener(this, arg -> saveButtonClickedAction());
		buttonLayout.addWidget(saveAnnotationButton);

		cancelShapeButton = new WPushButton("Cancel Shape");
		cancelShapeButton.disable();
		cancelShapeButton.clicked().addListener(this, arg -> cancelButtonClickedAction());
		buttonLayout.addWidget(cancelShapeButton);

		buttonPanel.setLayout(buttonLayout);
	}

	private void initializeAnnotationPanel() {
		annotationPanel = new WContainerWidget();
		annotationPanel.setMinimumSize(new WLength(300), WLength.Auto);
		annotationLayout = new WVBoxLayout();

		annotationsTable = new WTableView();
		annotationsTable.setModel(new TripleTableModel(annotationTriples));
		annotationsTable.setSelectionMode(SelectionMode.SingleSelection);
		annotationsTable.selectionChanged().addListener(this, () -> tableSelectionChangedAction());
		annotationLayout.addWidget(annotationsTable, 1);

		WContainerWidget tableControlPanel = new WContainerWidget();
		WHBoxLayout tableControlLayout = new WHBoxLayout();
		tableControlPanel.setLayout(tableControlLayout);

		addAnnotationButton = new WPushButton("Add");
		addAnnotationButton.enable();
		addAnnotationButton.clicked().addListener(this, arg -> addAnnotationButtonClickedAction());
		tableControlLayout.addWidget(addAnnotationButton);

		editAnnotationButton = new WPushButton("Edit");
		editAnnotationButton.disable();
		editAnnotationButton.clicked().addListener(this, arg -> editAnnotationButtonClickedAction());
		tableControlLayout.addWidget(editAnnotationButton);

		deleteAnnotationButton = new WPushButton("Delete");
		deleteAnnotationButton.disable();
		deleteAnnotationButton.clicked().addListener(this, arg -> deleteAnnotationButtonClickedAction());
		tableControlLayout.addWidget(deleteAnnotationButton);

		annotationLayout.addWidget(tableControlPanel);

		annotationEditorWidget = new TripleWidget();
		annotationEditorWidget.hide();
		annotationEditorWidget.setMinimumSize(WLength.Auto, new WLength(30));
		annotationEditorWidget.addPropertyChangeListener(this);
		annotationLayout.addWidget(annotationEditorWidget);

		annotationPanel.setLayout(annotationLayout);
	}

	private void initializeLayout() {
		WBorderLayout layout = new WBorderLayout();
		layout.addWidget(topPanel, WBorderLayout.Position.North);
		layout.addWidget(shapeContainer, WBorderLayout.Position.Center);
		layout.addWidget(annotationPanel, WBorderLayout.Position.East);

		setLayout(layout);
	}

	private void tableSelectionChangedAction() {
		if(annotationsTable.getSelectedIndexes().size() == 1) {
			editAnnotationButton.enable();
			deleteAnnotationButton.enable();
		}
	}

	private void addAnnotationButtonClickedAction() {
		annotationEditorWidget.resetData();
		annotationEditorWidget.show();
	}

	private void editAnnotationButtonClickedAction() {
		final WModelIndex selection = annotationsTable.getSelectedIndexes().first();
		annotationEditorWidget.setData(annotationTriples.get(selection.getRow()));
		annotationEditorWidget.show();
	}

	private void deleteAnnotationButtonClickedAction() {
		WMessageBox messageBox = new WMessageBox("Confirmation", "Do you want to delete this Annotation now?", Icon.Question, EnumSet.of(StandardButton.Yes, StandardButton.No));
		messageBox.buttonClicked().addListener(this, () -> 	deleteConfirmationButtonAction(messageBox));
		messageBox.show();
	}

	private void deleteConfirmationButtonAction(WMessageBox messageBox) {
		if(messageBox.getButtonResult() == StandardButton.Yes) {
			final WModelIndex selection = annotationsTable.getSelectedIndexes().first();
			LOGGER.info("removing selected element: {}", selection.getRow());
			annotationTriples.remove(selection.getRow());
			annotationsTable.setModel(new TripleTableModel(annotationTriples));

			editAnnotationButton.disable();
			deleteAnnotationButton.disable();
		}

		messageBox.remove();
	}

	private void mouseClickedOnImageAction(WMouseEvent arg) {
		if(isLeftMouseButton(arg) && isWidgetModeSetPoints()) {
			shapeWidget.addPointToShape(arg);
		}
		else if(isMiddleMouseButton(arg) && isShiftKeyPressed(arg)) {
			cancelButtonClickedAction();
		}
		else if(isMiddleMouseButton(arg) && (isWidgetModeSetPoints() || isWidgetModeUnsavedShape())) {
			saveButtonClickedAction();
		}
	}
	
	private void newButtonClickedAction() {
		shapeWidget.setWidgetMode(ShapeWidget.AnnotationWidgetMode.SET_POINTS);
	}

	private void cancelButtonClickedAction() {
		shapeWidget.resetShape();
	}

	private void saveButtonClickedAction() {
		if(isWidgetModeSetPoints()) {
			shapeWidget.confirmShape();
		}

		CreateShapeDialog dialog = new CreateShapeDialog(application);
		dialog.finished().addListener(this, () -> {
			if(dialog.getResult() == WDialog.DialogCode.Accepted) {
				saveShapeAndInitialTriplets(dialog);
			}

			dialog.remove();
		});

		dialog.show();
	}

	private void saveShapeAndInitialTriplets(CreateShapeDialog dialog) {
		LOGGER.info("saving initial annotations");

		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

		Concept selectedType = dialog.getTypeValue();
		String shapeID = imageAnnotationAPI.getNewSelectionShapeURI();

		Set<Triple> result = imageAnnotationAPI.saveNewShapeAndObject(
				sessionID, image.getIri(), shapeWidget.getUnsavedShape(), shapeID, selectedType.getIri(), dialog.getNameValue());

		shapeWidget.handleShapeSaved();

		annotationTriples.addAll(result);
		annotationsTable.setModel(new TripleTableModel(annotationTriples));

		LOGGER.info("initial save was successful");
	}

	private boolean isTripletValid() {
		List<WValidator.State> validationResults = annotationEditorWidget.validate();
		boolean entriesValid = true;
		for(WValidator.State state : validationResults) {
			if(state != WValidator.State.Valid) {
				entriesValid = false;
			}
		}
		if(!entriesValid) {
			String message = "Validation failed: ";
			int index = 0;
			for(WValidator.State state : validationResults) {
				index++;
				if(state != WValidator.State.Valid) {
					message += " field " + index + " ==> " + state + "     ";
				}
			}

			LOGGER.info(message);
			showErrorMessage(message);
			return false;
		}
		LOGGER.info("Validation successful");
		return true;
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
		else if(TripleWidget.CANCELLED_PROPERTY_NAME.equals(evt.getPropertyName())) {
			annotationEditorWidget.hide();
		}
		else if(TripleWidget.SAVED_PROPERTY_NAME.equals(evt.getPropertyName())) {
			if(isTripletValid()) {
				LOGGER.info(TripleWidget.SAVED_PROPERTY_NAME + " was triggered and triplet is valid --> saving to ontology");
				Triple triple = annotationEditorWidget.getData();

				// TODO save to ontology
				String sessionID = getSessionID();
				final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();
				//		imageAnnotationAPI.saveAnnotations(sessionID, );

				if(annotationTriples.contains(triple)) {
					// nothing?
				}
				else {
					annotationTriples.add(triple);
				}

				LOGGER.info("save was successful");

				// TODO try this less noisy
				annotationsTable.setModel(new TripleTableModel(annotationTriples));

				editAnnotationButton.disable();
				deleteAnnotationButton.disable();

				annotationEditorWidget.hide();
				annotationEditorWidget.resetData();
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

	private boolean isControlKeyPressed(WMouseEvent arg) {
		return arg.getModifiers().contains(KeyboardModifier.ControlModifier);
	}

	private boolean isWidgetModeSetPoints() {
		return ShapeWidget.AnnotationWidgetMode.SET_POINTS == shapeWidget.getWidgetMode();
	}

	private boolean isWidgetModeUnsavedShape() {
		return ShapeWidget.AnnotationWidgetMode.UNSAVED_SHAPE == shapeWidget.getWidgetMode();
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

	private void resetMessageField() {
		messageText.setText("");
		messageText.setStyleClass("");
	}

	private void loadAnnotations() {
		String sessionID = getSessionID();
		final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();
		Set<Triple> result = imageAnnotationAPI.getAllImageAnnotations(sessionID, image.getIri());

		annotationTriples = new ArrayList<>(result);
	}

	private String getSessionID() {
		return (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
	}

	private ImageAnnotationAPI getImageAnnotationAPI() {
		return (ImageAnnotationAPI) application.getServletContext().getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY);
	}
}
