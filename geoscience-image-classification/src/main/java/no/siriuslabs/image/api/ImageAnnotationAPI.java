package no.siriuslabs.image.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
import no.siriuslabs.image.model.URIUtils;
import no.siriuslabs.image.model.shape.AbstractShape;
import uio.ifi.ontology.toolkit.constraint.utils.Utility;
import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
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
			Set<String> locations = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance.getIri(), URIUtils.getURIForOntologyEntity(URIUtils.HAS_PHYISICAL_LOCATION_PROPERTY_NAME));
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
				URIUtils.getURIForOntologyEntity(URIUtils.HAS_PHYISICAL_LOCATION_PROPERTY_NAME), 
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
		String shape_type = URIUtils.getURIForOntologyEntity(
				StringUtils.capitalize(shape.getShapeType().toString().toLowerCase()));
		
		data_model.addTypeTriple(uri_shape, shape_type);
		
		
		//Link image with shape/selection
		data_model.addObjectTriple(uri_shape, URIUtils.getURIForOntologyEntity(URIUtils.IS_VISUALLY_REPRESENTED_PROPERTY_NAME), image_uri);
		
		//Add points to shape
		String point_uri;
		int i = 1;
		for (WPointF point : shape.getPoints()){
			point_uri = getNewResourceURI("point");
			
			//Type Point
			data_model.addTypeTriple(point_uri, URIUtils.getURIForOntologyEntity(URIUtils.POINT));
			
			//link between shape and points
			data_model.addObjectTriple(uri_shape, URIUtils.getURIForOntologyEntity(URIUtils.HASPOINT), point_uri);
			
			//coordinates
			data_model.addLiteralTriple(point_uri, URIUtils.getURIForOntologyEntity(URIUtils.HASX), String.valueOf(point.getX()), URIUtils.XSD_DOUBLE);
			data_model.addLiteralTriple(point_uri, URIUtils.getURIForOntologyEntity(URIUtils.HASY), String.valueOf(point.getY()), URIUtils.XSD_DOUBLE);
			
			//TODO for circles the order of points matters
			if (i==1) 
				data_model.addLiteralTriple(point_uri, URIUtils.getURIForOntologyEntity(URIUtils.ISMAINPOINT), "true", URIUtils.XSD_BOOLEAN);
			else
				data_model.addLiteralTriple(point_uri, URIUtils.getURIForOntologyEntity(URIUtils.ISMAINPOINT), "false", URIUtils.XSD_BOOLEAN);
			
			i++;
			
		}
		
		
		//Save new triples
		saveDataModel(data_model, session_id);
		
		
	}
	
	
	
	private void saveDataModel(AnnotationGraphModel data_model, String session_id) {
		
		try {
			
			String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
			data_model.saveModel(data_file_path);
			
			//Save tmp file with new annotations and perform incremental reasoning
			String tmp_file = Utility.tmp_directory + "tmp_file_annotations.ttl";
			data_model.saveNewAnnotationsModel(tmp_file);
			//Sync RDFox reasoner
			sessionManager.getSession(session_id).performMaterializationAdditionalData(tmp_file, true);//incremental reasoning
			
			
		} catch (Exception e) {
			LOGGER.error("Error storing the annotation model. Error: "+e.getMessage());
		}
		
	}
	
	
	
	private String getNewResourceURI(String base_name) {
		Random randomNum = new Random();
		int random = 1 + randomNum.nextInt(1000);
		return  URIUtils.getURIForResource(base_name + "-"+ random + Calendar.getInstance().getTimeInMillis());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

}
