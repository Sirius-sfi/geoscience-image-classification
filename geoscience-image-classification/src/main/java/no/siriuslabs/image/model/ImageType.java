package no.siriuslabs.image.model;

import uio.ifi.ontology.toolkit.projection.model.entities.Entity;

public class ImageType extends Entity{

	
	public String getPreferredTypeName() {
		return getName();
	}
	
	
	public String getTypeIdentifier() {
		return getIri();
	}
	
	
	
	

}
