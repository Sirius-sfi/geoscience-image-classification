package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.WAbstractTableModel;
import eu.webtoolkit.jwt.WModelIndex;
import no.siriuslabs.image.model.triples.Triple;
import uio.ifi.ontology.toolkit.projection.model.entities.Entity;
import uio.ifi.ontology.toolkit.projection.model.entities.LiteralValue;

import java.util.List;

/**
 * Table model of the annotation table.
 */
public class TripleTableModel extends WAbstractTableModel {

	private final List<Triple> data;

	public TripleTableModel(List<Triple> data) {
		this.data = data;
	}

	@Override
	public int getColumnCount(WModelIndex wModelIndex) {
		return 3;
	}

	@Override
	public int getRowCount(WModelIndex wModelIndex) {
		return data.size();
	}

	@Override
	public Object getData(WModelIndex index, int role) {
		if(ItemDataRole.DisplayRole == role) {
			Triple row = data.get(index.getRow());
			if(index.getColumn() == 0) {
				return row.getSubject().getVisualRepresentation();
			}
			else if(index.getColumn() == 1) {
				return ((Entity)row.getPredicate()).getVisualRepresentation();
			}
			else if(index.getColumn() == 2) {
				final Object rowObject = row.getObject();
				if(rowObject instanceof LiteralValue) {
					return ((LiteralValue)rowObject).getVisualRepresentation();
				}
				else if(rowObject instanceof Entity) {
					return ((Entity)rowObject).getVisualRepresentation();
				}
				else {
					return row.getObject();
				}
			}
		}
		return null;
	}

	@Override
	public Object getHeaderData(int section, Orientation orientation, int role) {
		if(orientation == Orientation.Horizontal) {
			if(ItemDataRole.DisplayRole == role) {
				if(section == 0) {
					return "Subject";
				}
				else if(section == 1) {
					return "Predicate";
				}
				else if(section == 2) {
					return "Object";
				}
			}
		}
		return null;
	}
}
