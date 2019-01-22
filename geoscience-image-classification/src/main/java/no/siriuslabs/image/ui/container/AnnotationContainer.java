package no.siriuslabs.image.ui.container;

import eu.webtoolkit.jwt.*;
import no.siriuslabs.image.FrontendApplication;
import no.siriuslabs.image.FrontendServlet;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GIC_URIUtils;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.ui.widget.CreateShapeDialog;
import no.siriuslabs.image.ui.widget.ShapeWidget;
import no.siriuslabs.image.ui.widget.TripleTreeTableNode;
import no.siriuslabs.image.ui.widget.TripleWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;
import uio.ifi.ontology.toolkit.projection.model.entities.Entity;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.LiteralValue;
import uio.ifi.ontology.toolkit.projection.model.entities.ObjectProperty;
import uio.ifi.ontology.toolkit.projection.model.triples.Triple;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	private WScrollArea shapeScrollArea;
	private WToolBar shapeToolBar;
	private ShapeWidget shapeWidget;
	private WPushButton newShapeButton;
	private WPushButton cancelShapeButton;
	private WPushButton saveAnnotationButton;

	private WVBoxLayout annotationLayout;
	private WTreeTable annotationsTable;
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
	 * List of tree nodes that describe the path to the last selected node as seen from the root.
	 */
	private List<WTreeNode> selectedPath = Collections.emptyList();

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
		initializeShapePanel();
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
		shapeWidget = new ShapeWidget(application, image);
		shapeWidget.setMinimumSize(new WLength(image.getWidth(), WLength.Unit.Pixel), new WLength(image.getHeight(), WLength.Unit.Pixel));
		shapeWidget.resize(image.getWidth(), image.getHeight()); // without this resize the widget seems to collapse to 0-size when put into the ScrollArea
		shapeWidget.addPropertyChangeListener(this);
		shapeWidget.mouseWentDown().addListener(this, arg -> mouseClickedOnImageAction(arg));
		shapeWidget.mouseWheel().addListener(this, (WMouseEvent arg) -> {
			if(arg.getWheelDelta() < 0) {
				decreaseImageZoomAction();
			}
			else if(arg.getWheelDelta() > 0) {
				increaseImageZoomAction();
			}
		});
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

		WPushButton zoomInButton = new WPushButton("+");
		zoomInButton.setToolTip("Zoom in");
		zoomInButton.clicked().addListener(this, (WMouseEvent arg) -> increaseImageZoomAction());
		shapeToolBar.addButton(zoomInButton);

		WPushButton zoomOutButton = new WPushButton("-");
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
		annotationPanel = new WContainerWidget();
		annotationPanel.setMinimumSize(new WLength(300), WLength.Auto);
		annotationLayout = new WVBoxLayout();

		annotationsTable = new WTreeTable();
		annotationsTable.getTree().setSelectionMode(SelectionMode.SingleSelection);
		annotationsTable.getTree().itemSelectionChanged().addListener(this, () -> tableSelectionChangedAction());

		annotationsTable.addColumn("Predicate", new WLength(150));
		annotationsTable.addColumn("Object", new WLength(150));

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

		annotationEditorWidget = new TripleWidget(application);
		annotationEditorWidget.hide();
		annotationEditorWidget.setMinimumSize(WLength.Auto, new WLength(30));
		annotationEditorWidget.addPropertyChangeListener(this);
		annotationLayout.addWidget(annotationEditorWidget);

		annotationPanel.setLayout(annotationLayout);

		populateTreeTable();
	}

	private void initializeLayout() {
		WBorderLayout layout = new WBorderLayout();
		layout.addWidget(topPanel, WBorderLayout.Position.North);
		layout.addWidget(shapeContainer, WBorderLayout.Position.Center);
		layout.addWidget(annotationPanel, WBorderLayout.Position.East);

		setLayout(layout);
	}

	private void populateTreeTable() {
				
		WTreeTableNode root = new WTreeTableNode(image.getLabel());
		annotationsTable.setTreeRoot(root, "Shape");

		String lastSubjectLabel = null;
		TripleTreeTableNode currentShapeNode = null;

		for(Triple triple : annotationTriples) {
			// did the subject change since last triple?
			if(!triple.getSubject().getVisualRepresentation().equals(lastSubjectLabel)) {
				// ...add new shape-level node
				currentShapeNode = new TripleTreeTableNode(triple.getSubject().getVisualRepresentation(), null, root, triple);
				currentShapeNode.setShapeNode(true);
				lastSubjectLabel = triple.getSubject().getVisualRepresentation();
			}

			//Ignore elements of give namespace
			if(getImageAnnotationAPI().getNamespaceToHideInVisualization().equals((((Entity)triple.getPredicate()).getNamespace()))) {
				continue;
			}

			// add annotation-level node
			TripleTreeTableNode node = new TripleTreeTableNode("", null, currentShapeNode, triple);

			node.setColumnWidget(1, new WText(((Entity)triple.getPredicate()).getVisualRepresentation()));

			final Object object = triple.getObject();
			String objectText = "";
			if(object instanceof LiteralValue) {
				objectText = ((LiteralValue)object).getVisualRepresentation();
			}
			else if(object instanceof Entity) {
				objectText = ((Entity)object).getVisualRepresentation();
			}
			else {
				objectText = triple.getObject().toString();
			}
			node.setColumnWidget(2, new WText(objectText));
		}

		annotationsTable.getTree().select(annotationsTable.getTreeRoot());
		annotationsTable.getTreeRoot().expand();
	}

	private void tableSelectionChangedAction() {
		// enable edit and delete buttons only if data is present and node is an annotation-level node
		if(annotationsTable.getTree().getSelectedNodes().size() == 1 && isTripleTreeTableNode(annotationsTable.getTree().getSelectedNodes().iterator().next())) {
			addAnnotationButton.enable();
			deleteAnnotationButton.enable();
			final TripleTreeTableNode selectedNode = (TripleTreeTableNode) annotationsTable.getTree().getSelectedNodes().iterator().next();
			if(selectedNode.isShapeNode()) {
				editAnnotationButton.disable();
			}
			else {
				editAnnotationButton.enable();
			}
		}
		else {
			addAnnotationButton.disable();
			editAnnotationButton.disable();
			deleteAnnotationButton.disable();
		}
	}

	private void addAnnotationButtonClickedAction() {
		annotationEditorWidget.resetData();
		annotationEditorWidget.setMode(TripleWidget.Mode.ADD);
		final TripleTreeTableNode selection = (TripleTreeTableNode) annotationsTable.getTree().getSelectedNodes().iterator().next();
		annotationEditorWidget.setSubject(selection.getData().getSubject());
		annotationEditorWidget.show();
	}

	private void editAnnotationButtonClickedAction() {
		annotationEditorWidget.setMode(TripleWidget.Mode.EDIT);
		final TripleTreeTableNode selection = (TripleTreeTableNode) annotationsTable.getTree().getSelectedNodes().iterator().next();
		annotationEditorWidget.setData(selection.getData());
		annotationEditorWidget.show();
	}

	private void deleteAnnotationButtonClickedAction() {
		WMessageBox messageBox;
		final TripleTreeTableNode selection = (TripleTreeTableNode) annotationsTable.getTree().getSelectedNodes().iterator().next();

		if(selection.isShapeNode()) {
			messageBox = new WMessageBox("Confirmation", "Do you want to delete this shape and " + selection.getChildNodes().size() + " annotations belonging to it now?", Icon.Question, EnumSet.of(StandardButton.Yes, StandardButton.No));
		}
		else {
			messageBox = new WMessageBox("Confirmation", "Do you want to delete this annotation now?", Icon.Question, EnumSet.of(StandardButton.Yes, StandardButton.No));
		}

		messageBox.buttonClicked().addListener(this, () -> 	deleteConfirmationButtonAction(messageBox));
		messageBox.show();
	}

	private void deleteConfirmationButtonAction(WMessageBox messageBox) {
		if(messageBox.getButtonResult() == StandardButton.Yes) {
			String sessionID = getSessionID();
			final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

			final TripleTreeTableNode selection = (TripleTreeTableNode) annotationsTable.getTree().getSelectedNodes().iterator().next();
			if(selection.isShapeNode()) {
				final Instance shapeObject = (Instance) selection.getData().getObject();
				final String shapeID = shapeObject.getVisualRepresentation();
				LOGGER.info("removing selected shape '{}' and {} annotations", shapeID, selection.getChildNodes().size());
				imageAnnotationAPI.removeShape(sessionID, shapeObject.getIri());
			}
			else {
				LOGGER.info("removing selected annotation: {}, {}", selection.getData().getSubject().getVisualRepresentation(), ((Entity)selection.getData().getPredicate()).getVisualRepresentation());
				imageAnnotationAPI.removeAnnotations(sessionID, Collections.singleton(selection.getData()));
			}

			saveSelection();
			loadAnnotations();
			populateTreeTable();
			restoreSelection();

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

		saveSelection();
		loadAnnotations();
		populateTreeTable();
		restoreSelection();

		LOGGER.info("initial save was successful");
	}

	private void saveSelection() {
		// we allow only single selection, so we only take the first element
		WTreeNode selectedNode = annotationsTable.getTree().getSelectedNodes().iterator().next();
		selectedPath = new ArrayList<>();

		addNodeToSelectedPath(selectedNode);
		Collections.reverse(selectedPath);
	}

	private void addNodeToSelectedPath(WTreeNode node) {
		if(!annotationsTable.getTreeRoot().equals(node)) {
			// if we haven't reached the tree root yet, we remember the current node go further up
			selectedPath.add(node);
			addNodeToSelectedPath(node.getParentNode());
		}
	}

	private void restoreSelection() {
		if(!selectedPath.isEmpty()) {
			findNextStepInSelectedPath(annotationsTable.getTreeRoot());
		}
	}

	private void findNextStepInSelectedPath(WTreeNode baseNode) {
		baseNode.expand();

		WTreeNode nodeInTree = null;
		WTreeNode nodeInPath = null;
		// node instances change because of reloading, so we have to identify the nodes to be selected/expanded by contents
		for(WTreeNode node : baseNode.getChildNodes()) {
			// we check if one of the children of baseNode matches the first element in the saved path to the selected node (the path is sorted from POV of the root, so the sequence fits)
			if(!selectedPath.isEmpty()) {
				WTreeNode pathNode = selectedPath.get(0);
				if(isTripleTreeTableNode(node) && isTripleTreeTableNode(pathNode)) {
					// for TripleTreeTableNodes we check the subject + predicate combination
					Triple nodeTriple = ((TripleTreeTableNode)node).getData();
					Triple pathTriple = ((TripleTreeTableNode)pathNode).getData();
					if(hasSameSubject(nodeTriple, pathTriple) && hasSamePredicate(nodeTriple, pathTriple)) {
						nodeInTree = node;
						nodeInPath = pathNode;
						break;
					}
				}
				else {
					// for standard nodes we check the node's label
					if(hasSameLabel(node, pathNode)) {
						nodeInTree = node;
						nodeInPath = pathNode;
						break;
					}
				}
			}
		}

		if(nodeInTree != null) {
			// if the next node towards the selected one was found, we remove it from the saved path
			selectedPath.remove(nodeInPath);

			annotationsTable.getTree().select(nodeInTree);
			// if there are more layers to do in the saved path, we do the same again for the next layer
			if(!selectedPath.isEmpty()) {
				findNextStepInSelectedPath(nodeInTree);
			}
		}
	}

	private boolean isTripleTreeTableNode(WTreeNode node) {
		return node instanceof TripleTreeTableNode;
	}

	private boolean hasSameSubject(Triple nodeTriple, Triple pathTriple) {
		return nodeTriple.getSubject().equalsEntity(pathTriple.getSubject());
	}

	private boolean hasSamePredicate(Triple nodeTriple, Triple pathTriple) {
		return nodeTriple.getPredicate().equals(pathTriple.getPredicate());
	}

	private boolean hasSameLabel(WTreeNode node, WTreeNode pathNode) {
		return node.getLabel().getText().equals(pathNode.getLabel().getText());
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
				LOGGER.info(TripleWidget.SAVED_PROPERTY_NAME + " was triggered and triple is valid --> saving to ontology");
				Triple triple = annotationEditorWidget.getData();

				String sessionID = getSessionID();
				final ImageAnnotationAPI imageAnnotationAPI = getImageAnnotationAPI();

				// delete the old triple if we are in edit-mode
				if(TripleWidget.Mode.EDIT == annotationEditorWidget.getMode()) {
					imageAnnotationAPI.removeAnnotations(sessionID, Collections.singleton(annotationEditorWidget.getOriginalData()));
				}

				imageAnnotationAPI.saveAnnotations(sessionID, Collections.singleton(triple));

				LOGGER.info("save was successful");

				saveSelection();
				loadAnnotations();
				populateTreeTable();
				restoreSelection();

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
		annotationTriples.sort(new TripleComparator());
	}

	private String getSessionID() {
		return (String) application.getServletContext().getAttribute(FrontendServlet.SESSION_ID_KEY);
	}

	private ImageAnnotationAPI getImageAnnotationAPI() {
		return (ImageAnnotationAPI) application.getServletContext().getAttribute(FrontendServlet.IMAGE_ANNOTATION_API_KEY);
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
}
