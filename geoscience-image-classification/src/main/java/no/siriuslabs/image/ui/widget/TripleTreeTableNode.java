package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.WIconPair;
import eu.webtoolkit.jwt.WTreeTableNode;
import uio.ifi.ontology.toolkit.projection.model.triples.Triple;
import uio.ifi.ontology.toolkit.projection.model.triples.TypeDefinitionTriple;

/**
 * Specialized TreeTableNode to hold Triple data in the node instead of Strings only.
 */
public class TripleTreeTableNode extends WTreeTableNode {

	private final Triple data;

	private boolean shapeNode = false;

	/**
	 * TreeTableNode constructor with an added parameter for a Triplet holding data.
	 */
	public TripleTreeTableNode(CharSequence labelText, WIconPair labelIcon, WTreeTableNode parentNode, Triple data) {
		super(labelText, labelIcon, parentNode);
		this.data = data;
	}

	/**
	 * TreeTableNode constructor with an added parameter for a Triplet holding data.
	 */
	public TripleTreeTableNode(CharSequence labelText, Triple data) {
		super(labelText);
		this.data = data;
	}

	/**
	 * TreeTableNode constructor with an added parameter for a Triplet holding data.
	 */
	public TripleTreeTableNode(CharSequence labelText, WIconPair labelIcon, Triple data) {
		super(labelText, labelIcon);
		this.data = data;
	}

	/**
	 * Returns the Triplet for this node.
	 */
	public Triple getData() {
		return data;
	}

	/**
	 * Returns true if this node represents a TypeDefinitionTriple.
	 */
	public boolean isTypeNode() {
		return data instanceof TypeDefinitionTriple;
	}

	/**
	 * Returns if this node represents a shape (which implies it can have child nodes and is treated special in navigation).
	 */
	public boolean isShapeNode() {
		return shapeNode;
	}

	/**
	 * Sets if this node represents a shape (which implies it can have child nodes and is treated special in navigation).
	 */
	public void setShapeNode(boolean shapeNode) {
		this.shapeNode = shapeNode;
	}
}
