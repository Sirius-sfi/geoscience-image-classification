package no.siriuslabs.image.model;

import no.siriuslabs.image.model.shape.ShapeType;

public class GIC_URIUtils {
	
	//This could potentially be added to the configuration file web services too.
	public static final String BASE_URI_ONTOLOGY  = "http://no.sirius.ontology/ontology";
	public static final String BASE_URI_RESOURCES = "http://no.sirius.ontology/res";
	public static final String BASE_URI_ANNOTATIONS = "http://no.sirius.ontology/ann";
	
	
	
	public static final String HAS_PROVENANCE= "hasProvenance";
	public static final String HAS_PHYISICAL_LOCATION= "hasPhysicalLocation";
	
	public static final String IS_SELECTION_OF= "isSelectionOf";
	public static final String HAS_SELECTION= "hasSelection";
	
	public static final String IS_VISUALLY_REPRESENTED= "isVisuallyRepresentedIn";
	public static final String HAS_OBJECT_REPRESENTATION= "hasObjectRepresentation";
	
	
	public static final String CIRCLE= "Circle";
	public static final String RECTANGLE= "Rectangle";
	public static final String TRIANGLE= "Triangle";
	public static final String POLYGON= "Polygon";
	
	
	public static final String IMAGE_SELECTION= "ImageSelection";
	public static final String POINT= "Point";
	
	
	public static final String HASPOINT= "hasPoint";
	
	public static final String HASX= "hasX";
	public static final String HASY= "hasY";
	
	public static final String HASPOINTORDER = "hasOrder";

	public static final String SHAPE_OBJECT_PREFIX = "shape";


	//TODO URIS relevant to other domains
	
	
	
	
	
	
	

	public static String getURIForAnnotationOntologyEntity(String name_entity) {
		return BASE_URI_ANNOTATIONS + "#" + name_entity;
	}
	
	public static String getURIForResource(String name_resource) {
		return BASE_URI_RESOURCES + "#" + name_resource;
	}
	
	
	
	public static void main (String[] args) {
		System.out.println(ShapeType.CIRCLE);
		
		System.out.println(ShapeType.CIRCLE.getNumberOfPoints());
	}

}
