package no.siriuslabs.image.ui.widget;

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.WAbstractTableModel;
import eu.webtoolkit.jwt.WModelIndex;

import java.util.List;

/**
 * Table model of the annotation table.
 */
public class TripletTableModel extends WAbstractTableModel {

	private final List<TripletPlaceholder> triplets;

	public TripletTableModel(List<TripletPlaceholder> triplets) {
		this.triplets = triplets;
	}

	@Override
	public int getColumnCount(WModelIndex wModelIndex) {
		return 3;
	}

	@Override
	public int getRowCount(WModelIndex wModelIndex) {
		return triplets.size();
	}

	@Override
	public Object getData(WModelIndex index, int role) {
		if(ItemDataRole.DisplayRole == role) {
			TripletPlaceholder row = triplets.get(index.getRow());
			if(index.getColumn() == 0) {
				return row.getSubject();
			}
			else if(index.getColumn() == 1) {
				return row.getPredicate();
			}
			else if(index.getColumn() == 2) {
				return row.getObject();
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
