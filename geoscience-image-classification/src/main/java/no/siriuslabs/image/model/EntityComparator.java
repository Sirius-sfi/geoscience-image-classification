package no.siriuslabs.image.model;

import uio.ifi.ontology.toolkit.projection.model.entities.Entity;

import java.util.Comparator;

/**
 * Comparator to sort Entity instances alphabetically by the visual representation.
 */
public class EntityComparator implements Comparator<Entity> {

	@Override
	public int compare(Entity o1, Entity o2) {
		if(o1 == null) {
			return 1;
		}
		if(o2 == null) {
			return -1;
		}
		return o1.getVisualRepresentation().compareTo(o2.getVisualRepresentation());
	}
}
