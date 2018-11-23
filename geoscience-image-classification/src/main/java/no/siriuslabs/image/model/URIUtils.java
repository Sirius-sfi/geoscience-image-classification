package no.siriuslabs.image.model;

import no.siriuslabs.image.model.shape.ShapeType;

public class URIUtils {
	
	//This could potentially be added to the configuration file web services too.
	public static final String BASE_URI_GEOLOGICAL_ONTOLOGY  = "http://no.sirius.ontology/gico";
	public static final String BASE_URI_GEOLOGICAL_RESOURCES = "http://no.sirius.ontology/gicr";
	
	
	public static final String HAS_PROVENANCE_PROPERTY_NAME= "hasProvenance";
	public static final String HAS_PHYISICAL_LOCATION_PROPERTY_NAME= "hasPhysicalLocation";
	
	public static final String IS_VISUALLY_REPRESENTED_PROPERTY_NAME= "isVisuallyRepresentedIn";
	public static final String HAS_VISUAL_REPRESENTATION_PROPERTY_NAME= "hasObjectRepresentation";
	
	public static final String POINT= "Point";
	
	
	public static final String HASPOINT= "hasPoint";
	
	public static final String HASY= "hasX";
	public static final String HASX= "hasY";
	
	public static final String ISMAINPOINT= "isMainPoint";
	
	
	
	
	//TODO URIS relevant to other domains
	public static String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
	public static String XSD_DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
	public static String XSD_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
	
	
	

	public static String getURIForOntologyEntity(String name_entity) {
		return BASE_URI_GEOLOGICAL_ONTOLOGY + "#" + name_entity;
	}
	
	public static String getURIForResource(String name_resource) {
		return BASE_URI_GEOLOGICAL_RESOURCES + "#" + name_resource;
	}
	
	
	
	public static void main (String[] args) {
		System.out.println(ShapeType.CIRCLE);
		
		System.out.println(ShapeType.CIRCLE.getNumberOfPoints());
	}

}
