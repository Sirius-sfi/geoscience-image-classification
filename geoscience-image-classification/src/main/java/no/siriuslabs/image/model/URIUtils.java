package no.siriuslabs.image.model;

public class URIUtils {
	
	//This could potentially be added to the configuration file web services too.
	public static final String BASE_URI_GEOLOGICAL_ONTOLOGY  = "http://no.sirius.ontology/gico";
	public static final String BASE_URI_GEOLOGICAL_RESOURCES = "http://no.sirius.ontology/gicr";
	
	
	public static final String HAS_PROVENANCE_PROPERTY_NAME= "hasProvenance";
	public static final String HAS_PHYISICAL_LOCATION_PROPERTY_NAME= "hasPhysicalLocation";
	
	public static final String IS_VISUALLT_REPRESENTED_PROPERTY_NAME= "isVisuallyRepresentedIn";
	public static final String HAS_VISUAL_REPRESENTATION_PROPERTY_NAME= "hasVisualRepresentation";
	
	
	//TODO URIS relevant to other domains
	
	
	

	public static String getOntologyURI(String entity) {
		return BASE_URI_GEOLOGICAL_ONTOLOGY + "#" + entity;
	}

}
