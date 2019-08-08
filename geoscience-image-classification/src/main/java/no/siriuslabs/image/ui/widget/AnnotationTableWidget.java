package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.Icon;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTreeNode;
import eu.webtoolkit.jwt.WTreeTable;
import eu.webtoolkit.jwt.WTreeTableNode;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WValidator;
import no.siriuslabs.image.api.ImageAnnotationAPI;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.ui.container.AnnotationContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uio.ifi.ontology.toolkit.projection.model.entities.Entity;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.LiteralValue;
import uio.ifi.ontology.toolkit.projection.model.triples.Triple;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Widget consisting of a tree-table to display annotations and controls and a TripleWidget to add, update and delete these annotations.
 */
public class AnnotationTableWidget extends AbstractAnnotationWidget implements PropertyChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationTableWidget.class);

	public static final String SELECTED_SHAPE_PROPERTY_NAME = "AnnotationTableWidget.currentSelectedShape";
	public static final String SHAPE_DATA_CHANGED_PROPERTY_NAME = "AnnotationTableWidget.shapesChanged";

	private final PropertyChangeSupport propertyChangeSupport;

	private final GeologicalImage image;

	private WTreeTable annotationsTable;
	private TripleWidget annotationEditorWidget;

	private WContainerWidget tableControlPanel;
	private WPushButton addAnnotationButton;
	private WPushButton editAnnotationButton;
	private WPushButton deleteAnnotationButton;

	/**
	 * List of tree nodes that describe the path to the last selected node as seen from the root.
	 */
	private List<WTreeNode> selectedPath = Collections.emptyList();

	private List<Triple> annotationTriples;

	/**
	 * Constructor taking the parent container and the GeologicalImage that is to be annotated.
	 */
	public AnnotationTableWidget(AnnotationContainer parent, GeologicalImage image) {
		super(parent);
		this.image = image;

		propertyChangeSupport = new PropertyChangeSupport(this);

		setMinimumSize(new WLength(300), WLength.Auto);

		initializeTable();
		initializeTableControls();
		initializeEditorWidget();
		initializeLayout();

		annotationTriples = parent.loadAnnotations();
		populateTreeTable();
	}

	private void initializeTable() {
		annotationsTable = new WTreeTable();
		annotationsTable.getTree().setSelectionMode(SelectionMode.SingleSelection);
		annotationsTable.getTree().itemSelectionChanged().addListener(this, () -> tableSelectionChangedAction());

		annotationsTable.addColumn("Predicate", new WLength(150));
		annotationsTable.addColumn("Object", new WLength(150));
	}

	private void initializeTableControls() {
		tableControlPanel = new WContainerWidget();
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
	}

	private void initializeEditorWidget() {
		annotationEditorWidget = new TripleWidget(getParentContainer());
		annotationEditorWidget.hide();
		annotationEditorWidget.setMinimumSize(WLength.Auto, new WLength(30));
		annotationEditorWidget.addPropertyChangeListener(this);
	}

	private void initializeLayout() {
		WVBoxLayout annotationLayout = new WVBoxLayout();
		annotationLayout.addWidget(annotationsTable, 1);
		annotationLayout.addWidget(tableControlPanel);
		annotationLayout.addWidget(annotationEditorWidget);
		setLayout(annotationLayout);
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
				propertyChangeSupport.firePropertyChange(SELECTED_SHAPE_PROPERTY_NAME, null, (Instance) selectedNode.getData().getObject());
			}
			else if(selectedNode.isTypeNode()) {
				editAnnotationButton.disable();
				propertyChangeSupport.firePropertyChange(SELECTED_SHAPE_PROPERTY_NAME, null, null);
			}
			else {
				editAnnotationButton.enable();
				propertyChangeSupport.firePropertyChange(SELECTED_SHAPE_PROPERTY_NAME, null, null);
			}
		}
		else {
			addAnnotationButton.disable();
			editAnnotationButton.disable();
			deleteAnnotationButton.disable();
			propertyChangeSupport.firePropertyChange(SELECTED_SHAPE_PROPERTY_NAME, null, null);
		}
	}

	public void addAnnotationButtonClickedAction() {
		if(addAnnotationButton.isEnabled()) {
			annotationEditorWidget.resetData();
			annotationEditorWidget.setMode(TripleWidget.Mode.ADD);
			final TripleTreeTableNode selection = (TripleTreeTableNode) annotationsTable.getTree().getSelectedNodes().iterator().next();
			annotationEditorWidget.setSubject(selection.getData().getSubject());
			annotationEditorWidget.show();
		}
	}

	public void editAnnotationButtonClickedAction() {
		if(editAnnotationButton.isEnabled()) {
			annotationEditorWidget.setMode(TripleWidget.Mode.EDIT);
			final TripleTreeTableNode selection = (TripleTreeTableNode) annotationsTable.getTree().getSelectedNodes().iterator().next();
			annotationEditorWidget.setData(selection.getData());
			annotationEditorWidget.show();
		}
	}

	public void deleteAnnotationButtonClickedAction() {
		if(deleteAnnotationButton.isEnabled()) {
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
				propertyChangeSupport.firePropertyChange(SHAPE_DATA_CHANGED_PROPERTY_NAME, null, null);
			}
			else {
				LOGGER.info("removing selected annotation: {}, {}", selection.getData().getSubject().getVisualRepresentation(), ((Entity)selection.getData().getPredicate()).getVisualRepresentation());
				imageAnnotationAPI.removeAnnotations(sessionID, Collections.singleton(selection.getData()));
			}

			refreshData();

			editAnnotationButton.disable();
			deleteAnnotationButton.disable();
		}

		messageBox.remove();
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

	/**
	 * Reacts to changes in child widgets.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		LOGGER.info("Handling event: {}", evt.getPropertyName());

		if(TripleWidget.CANCELLED_PROPERTY_NAME.equals(evt.getPropertyName())) {
			((AnnotationContainer)getParentContainer()).clearMessageField();
			annotationEditorWidget.hide();
		}
		else if(TripleWidget.VALIDATE_PROPERTY_NAME.equals(evt.getPropertyName())) {
			List<WValidator.State> validationResults = annotationEditorWidget.validate();
			String message = "";
			for(WValidator.State state : validationResults) {
				if(state == WValidator.State.Invalid) {
					message = "Only one element is allowed";
				}
			}

			if(message.isEmpty()) {
				((AnnotationContainer)getParentContainer()).clearMessageField();
			}
			else {
				LOGGER.info(message);
				((AnnotationContainer)getParentContainer()).showErrorMessage(message);
			}
		}
		else if(TripleWidget.SAVED_PROPERTY_NAME.equals(evt.getPropertyName())) {
			if(isTripleValid()) {
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

				refreshData();

				editAnnotationButton.disable();
				deleteAnnotationButton.disable();

				annotationEditorWidget.hide();
				annotationEditorWidget.resetData();
			}
		}
	}

	/**
	 * Triggers a reload of the data from the ontology using the parent's loadAnnotations()-method and updates the tree-table while keeping the active selection if possible.
	 */
	public void refreshData() {
		saveSelection();
		annotationTriples = ((AnnotationContainer)getParentContainer()).loadAnnotations();
		populateTreeTable();
		restoreSelection();
	}

	private boolean isTripleValid() {
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
			((AnnotationContainer)getParentContainer()).showErrorMessage(message);
			return false;
		}
		LOGGER.info("Validation successful");
		return true;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public void resetTableSelection() {
		annotationsTable.getTree().select(annotationsTable.getTreeRoot());
	}
}
