package no.siriuslabs.image;

import eu.webtoolkit.jwt.KeyboardModifier;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WBorderLayout;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WValidator;
import no.siriuslabs.image.model.GeologicalImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Container class representing the annotation page accessed from the main menu.
 */
public class AnnotationContainer extends WContainerWidget implements PropertyChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationContainer.class);

	private final FrontendApplication application;
	private final GeologicalImage image;

	private ShapeWidget shapeWidget;
	private WPushButton newShapeButton;
	private WPushButton cancelShapeButton;
	private WPushButton confirmShapeButton;
	private WPushButton saveAnnotationButton;

	private TripletWidget newAnnotationWidget;
	private WVBoxLayout annotationLayout;

	private WContainerWidget topPanel;
	private WContainerWidget annotationPanel;
	private WContainerWidget buttonPanel;

	private WText messageText;

	private List<TripletWidget> triplets;

	/**
	 * Constructor taking the application, parent container and the GeologicalImage that is to be annotated.
	 */
	public AnnotationContainer(FrontendApplication application, WContainerWidget parent, GeologicalImage image) {
		super(parent);
		LOGGER.info("{} constructor - start", getClass().getSimpleName());

		this.application = application;
		this.image = image;

		triplets = new ArrayList<>();

		initializeTopPanel();
		initializeShapeWidget();
		initializeButtonPanel();
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
		shapeWidget = new ShapeWidget(application, image);
		shapeWidget.setMinimumSize(new WLength(800, WLength.Unit.Pixel), new WLength(600, WLength.Unit.Pixel));
		shapeWidget.addPropertyChangeListener(this);

		shapeWidget.mouseWentDown().addListener(this, arg -> mouseClickedOnImageAction(arg));
	}

	private void initializeButtonPanel() {
		buttonPanel = new WContainerWidget();
		WHBoxLayout buttonLayout = new WHBoxLayout();

		newShapeButton = new WPushButton("New Shape");
		newShapeButton.clicked().addListener(this, arg -> newButtonClickedAction());
		buttonLayout.addWidget(newShapeButton);

		cancelShapeButton = new WPushButton("Cancel");
		cancelShapeButton.disable();
		cancelShapeButton.clicked().addListener(this, arg -> cancelButtonClickedAction());
		buttonLayout.addWidget(cancelShapeButton);

		confirmShapeButton = new WPushButton("Confirm");
		confirmShapeButton.disable();
		confirmShapeButton.clicked().addListener(this, arg -> confirmButtonClickedAction());
		buttonLayout.addWidget(confirmShapeButton);

		saveAnnotationButton = new WPushButton("Save");
		saveAnnotationButton.disable();
		saveAnnotationButton.clicked().addListener(this, arg -> saveButtonClickedAction());
		buttonLayout.addWidget(saveAnnotationButton);

		buttonPanel.setLayout(buttonLayout);
	}

	// TODO rework with table...
	private void initializeAnnotationPanel() {
		annotationPanel = new WContainerWidget();
		annotationPanel.setMinimumSize(new WLength(300), WLength.Auto);
		annotationLayout = new WVBoxLayout();

		newAnnotationWidget = new TripletWidget();
		newAnnotationWidget.disable();
		newAnnotationWidget.setMinimumSize(WLength.Auto, new WLength(30));
		annotationLayout.addWidget(newAnnotationWidget);

		annotationLayout.addWidget(new WText(), 1);

		annotationPanel.setLayout(annotationLayout);
	}

	private void initializeLayout() {
		WBorderLayout layout = new WBorderLayout();
		layout.addWidget(topPanel, WBorderLayout.Position.North);
		layout.addWidget(shapeWidget, WBorderLayout.Position.Center);
		layout.addWidget(annotationPanel, WBorderLayout.Position.East);
		layout.addWidget(buttonPanel, WBorderLayout.Position.South);

		setLayout(layout);
	}

	private void mouseClickedOnImageAction(WMouseEvent arg) {
		if(isLeftMouseButton(arg) && isWidgetModeSetPoints()) {
			shapeWidget.addPointToShape(arg);
		}
		else if(isMiddleMouseButton(arg) && isShiftKeyPressed(arg)) {
			cancelButtonClickedAction();
		}
		else if(isMiddleMouseButton(arg) && isControlKeyPressed(arg) && isWidgetModeUnsavedShape()) {
			saveButtonClickedAction();
		}
		else if(isMiddleMouseButton(arg) && isWidgetModeSetPoints()) {
			confirmButtonClickedAction();
		}
	}

	private void newButtonClickedAction() {
		shapeWidget.setWidgetMode(ShapeWidget.AnnotationWidgetMode.SET_POINTS);
	}

	private void cancelButtonClickedAction() {
		shapeWidget.resetShape();
	}

	private void confirmButtonClickedAction() {
		shapeWidget.confirmShape();

		newAnnotationWidget.enable();
	}

	private void saveButtonClickedAction() {
		if(isWidgetModeUnsavedShape()) {
			LOGGER.info("saving widget");
			shapeWidget.saveShape();
		}

		LOGGER.info("saving annotation");

		List<WValidator.State> validationResults = newAnnotationWidget.validate();
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
			return;
		}

		LOGGER.info("1: " + newAnnotationWidget.getPart1Value() + "   2: " + newAnnotationWidget.getPart2Value() + "  3: " + newAnnotationWidget.getPart3Value());
		// TODO save annotation to ontology

		triplets.add(newAnnotationWidget);
		newAnnotationWidget.disable();

		newAnnotationWidget = new TripletWidget();
		newAnnotationWidget.setMinimumSize(WLength.Auto, new WLength(30));
		annotationLayout.insertWidget(triplets.size(), newAnnotationWidget);
	}

	/**
	 * Reacts to changes of the widgetMode property in the ShapeWidget.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if("widgetMode".equals(evt.getPropertyName())) {
			final ShapeWidget.AnnotationWidgetMode widgetMode = (ShapeWidget.AnnotationWidgetMode) evt.getNewValue();
			LOGGER.info("widgetMode changed to " + widgetMode);

			if(widgetMode == ShapeWidget.AnnotationWidgetMode.SET_POINTS) {
				newShapeButton.disable();
				cancelShapeButton.enable();
				confirmShapeButton.enable();
				saveAnnotationButton.disable();
			}
			else if(widgetMode == ShapeWidget.AnnotationWidgetMode.UNSAVED_SHAPE) {
				newShapeButton.disable();
				cancelShapeButton.enable();
				confirmShapeButton.disable();
				saveAnnotationButton.enable();
			}
			else if(widgetMode == ShapeWidget.AnnotationWidgetMode.SAVED_SHAPE) {
				newShapeButton.enable();
				cancelShapeButton.disable();
				confirmShapeButton.disable();
				saveAnnotationButton.enable();
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
}
