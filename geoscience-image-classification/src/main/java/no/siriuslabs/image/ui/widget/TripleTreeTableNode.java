package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.WIconPair;
import eu.webtoolkit.jwt.WTreeTableNode;
import uio.ifi.ontology.toolkit.projection.model.triples.Triple;

/**
 * Specialized TreeTableNode to hold Triple data in the node instead of Strings only.
 */
public class TripleTreeTableNode extends WTreeTableNode {

	private final Triple data;

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
}
