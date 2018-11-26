package no.siriuslabs.image.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.WPointF;
import no.siriuslabs.image.model.AnnotationGraphModel;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.model.GIC_URIUtils;
import no.siriuslabs.image.model.shape.AbstractShape;
import no.siriuslabs.image.model.shape.Circle;
import no.siriuslabs.image.model.shape.Polygon;
import no.siriuslabs.image.model.shape.Rectangle;
import no.siriuslabs.image.model.shape.ShapeType;
import no.siriuslabs.image.model.shape.Triangle;
import uio.ifi.ontology.toolkit.constraint.utils.Utility;
import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.utils.URIUtils;
import uio.ifi.ontology.toolkit.projection.view.OntologyProjectionAPI;



public class ImageAnnotationAPI extends OntologyProjectionAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyProjectionAPI.class);
	
	
	public ImageAnnotationAPI(RDFoxSessionManager session){
		sessionManager = session;
	}
	
	
	
	public double getRandomNumber() {
		return Math.random();
	}
	
	
	public int getNumberLoadedOntologies() {
		return sessionManager.getLoadedOntologies().size();
	}
	
	
	public void createNewSession(String iri, String data_file) {
		sessionManager.createNewSession(iri, data_file);
	}
	
	
	//First element top class
	public List<Concept> getImageTypes(String session_id){
		//TODO Query for special annotation in ontology	
		List<Concept> imageTypes = new ArrayList<Concept>();
		
		Concept mainArtefact = sessionManager.getSession(session_id).getMainArtefactConcept();
		imageTypes.add(mainArtefact);
		
				
		TreeSet<Concept> concepts = sessionManager.getSession(session_id).getAllSubClasses(mainArtefact.getIri());
		for (Concept c : concepts.descendingSet()){
			imageTypes.add(c);
		}
		
		
		return imageTypes;
	}
	
	
	public List<GeologicalImage> getImagesOfGivenType(String session_id, String type){
		
		//Convert string type to URI
		Concept type_concept = sessionManager.getSession(session_id).getConceptForLabel(type);
		
		
		//Queries 1: Images of type X and then get location
		TreeSet<Instance> instances = sessionManager.getSession(session_id).getInstancesForType(type_concept.getIri());
		
		List<GeologicalImage> images = new ArrayList<>(instances.size());
		
		
		//Create GeologicalImage objects and location
		for (Instance instance: instances) {
			
			GeologicalImage geoImage = new GeologicalImage(instance);
			//Get location: get ?o given "s" and "p" : generic getObjets SPARQL query: unary results
			
			//There should be only one location
			Set<String> locations = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance.getIri(), GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HAS_PHYISICAL_LOCATION_PROPERTY_NAME));
			for(String location: locations) {
				geoImage.setLocation(location);
			}
			
			//Add label for type
			//We keep only one
			Set<String> labels = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(geoImage.getClassType(), URIUtils.RDFS_LABEL);
			for(String type_label: labels) {
				geoImage.setTypeLabel(type_label);
			}
						
			images.add(geoImage);
			
			//Utility.println(geoImage.toJSON().toString(2));
			//Utility.println("Location: "+geoImage.getLocation());
			
		}

		Collections.sort(images, new Comparator<GeologicalImage>() {
			@Override
			public int compare(GeologicalImage o1, GeologicalImage o2) {
				if(o1.getClassType() == null) {
					return -1;
				}
				if(o2.getClassType() == null) {
					return 1;
				}

				return o1.getClassType().compareTo(o2.getClassType());
			}
		});

		return images;
		
	}
	
	
	
	
	public List<WPointF> getPointsForShape(String session_id, String shape_uri){
		
		List<WPointF> points = new ArrayList<WPointF>();
		
		//1. Get Points (object) for a shape
		Set<String> point_uris = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(shape_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASPOINT));
		
		for (String point_uri : point_uris) {
			
			WPointF point = new WPointF();
			
			//coordinate x
			Set<String> values = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASX));
			for (String value: values) {//only one expected
				point.setX(Double.valueOf(value));
			}
			
			//coordinate y
			values = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASY));
			for (String value: values) {//only one expected
				point.setY(Double.valueOf(value));
			}
			
			values = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.ISMAINPOINT));
			//if values.size()>0 then main point
			
			if (values.isEmpty())
				points.add(point);
			else
				points.add(0, point);  //no empty then main point should be the first
			
		}
		
		
		
		
		return points;
		
	}
	
	
	
	
	public Set<AbstractShape> getSelectionShapesForImage(String session_id, String image_uri){
		
		
		Set<AbstractShape> shapes = new HashSet<AbstractShape>();
		
		
		//1. Get shapes (subject) visually represented in image
		Set<String> shape_uris = sessionManager.getSession(session_id).getSubjectsForObjectPredicate(GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED_PROPERTY_NAME), image_uri);
		
		//Utility.println(shape_uris.size() + " shapes for " +image_uri);
		
		
		for (String uri_shape : shape_uris) {
			
			AbstractShape shape;
			
			
			//Get semantic type
			String type = sessionManager.getSession(session_id).getMostConcreteTypeForInstance(uri_shape);
			if (type.equals(""))
				type = URIUtils.OWLTHING;
			
			//Get points
			switch (URIUtils.getEntityLabelFromURI(type)) {
				case GIC_URIUtils.CIRCLE:
					shape = new Circle(getPointsForShape(session_id, uri_shape));
					break;
				case GIC_URIUtils.RECTANGLE:
					shape = new Rectangle(getPointsForShape(session_id, uri_shape));
					break;				
				case GIC_URIUtils.TRIANGLE:
					shape = new Triangle(getPointsForShape(session_id, uri_shape));
					break;
				case GIC_URIUtils.POLYGON:
					shape = new Polygon(getPointsForShape(session_id, uri_shape));
					break;
				default: 
					shape = new Circle(getPointsForShape(session_id, uri_shape));
					LOGGER.warn("Shape without a type: "+ uri_shape);
                	break;
			}
					
			shape.setClassType(type);
			shape.setIri(uri_shape);
			
			shapes.add(shape);
			
			//Utility.println(shape.toJSON().toString(2));
			
		}
		
		
		
		
		return shapes;
		
	}
	
	
	
	
	
	
	public void saveGeologicalImage(String session_id, GeologicalImage gimg) {
		//Type is the label in selection box, get real URI
		//LOGGER.info(gimg.toString());
		Concept type_concept = sessionManager.getSession(session_id).getConceptForLabel(gimg.getClassType());
		gimg.setClassType(type_concept.getIri());
		
		
		//Store img annotation model		
		AnnotationGraphModel data_model = new AnnotationGraphModel();
		
		//Load model
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
		
		data_model.addTypeTriple(gimg.getIri(), gimg.getClassType());
		data_model.addLabelTriple(gimg.getIri(), gimg.getLabel());
		data_model.addCommentTriple(gimg.getIri(), gimg.getDescription());
		//location
		data_model.addLiteralTriple(
				gimg.getIri(), 
				GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HAS_PHYISICAL_LOCATION_PROPERTY_NAME), 
				gimg.getLocation());
		
		
		
		
		//Save new triples
		saveDataModel(data_model, session_id);
		
	}
	
	
	
	public void saveNewShape(String session_id, String image_uri, AbstractShape shape) {
	
		//Store img annotation model		
		AnnotationGraphModel data_model = new AnnotationGraphModel();
				
		
		//Load model
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
		
		
		
		//Create URI for shape
		String uri_shape = getNewResourceURI("shape");
		
		//Assign URI type depending on the shapeType: Trianle, Circle...
		String shape_type = GIC_URIUtils.getURIForOntologyEntity(
				StringUtils.capitalize(shape.getShapeType().toString().toLowerCase()));
		
		data_model.addTypeTriple(uri_shape, shape_type);
		
		
		//Link image with shape/selection
		data_model.addObjectTriple(uri_shape, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED_PROPERTY_NAME), image_uri);
		
		//Add points to shape
		String point_uri;
		int i = 1;
		for (WPointF point : shape.getPoints()){
			point_uri = getNewResourceURI("point");
			
			//Type Point
			data_model.addTypeTriple(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.POINT));
			
			//link between shape and points
			data_model.addObjectTriple(uri_shape, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASPOINT), point_uri);
			
			//coordinates
			data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASX), String.valueOf(point.getX()), URIUtils.XSD_DOUBLE);
			data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASY), String.valueOf(point.getY()), URIUtils.XSD_DOUBLE);
			
			//TODO for circles the order of points matters
			if (i==1) 
				data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.ISMAINPOINT), "true", URIUtils.XSD_BOOLEAN);
			//else
			//	data_model.addLiteralTriple(point_uri, URIUtils.getURIForOntologyEntity(URIUtils.ISMAINPOINT), "false", URIUtils.XSD_BOOLEAN); //no need to store for false cases
			
			i++;
			
		}
		
		
		//Save new triples
		saveDataModel(data_model, session_id);
		
		
		
		
	}
	
	
	/**
	 * Saves models and clears structures
	 * @param data_model
	 * @param session_id
	 */
	private void saveDataModel(AnnotationGraphModel data_model, String session_id) {
		
		try {
			
			String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
			data_model.saveModel(data_file_path);
			
			//Save tmp file with new annotations and perform incremental reasoning
			String tmp_file = Utility.tmp_directory + "tmp_file_annotations.ttl";
			data_model.saveNewAnnotationsModel(tmp_file);
			//Sync RDFox reasoner
			sessionManager.getSession(session_id).performMaterializationAdditionalData(tmp_file, true);//incremental reasoning
			
			
			//dispose/clear models
			data_model.dispose();
			
			
		} catch (Exception e) {
			LOGGER.error("Error storing the annotation model. Error: "+e.getMessage());
		}
		
	}
	
	
	
	private String getNewResourceURI(String base_name) {
		Random randomNum = new Random();
		int random = 1 + randomNum.nextInt(1000);
		return  GIC_URIUtils.getURIForResource(base_name + "-"+ random + Calendar.getInstance().getTimeInMillis());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

}
