package no.siriuslabs.image.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.siriuslabs.image.model.AnnotationGraphModel;
import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.model.URIUtils;
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
			Set<String> labels = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(geoImage.getType(), URIUtils.RDFS_LABEL);
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
				if(o1.getType() == null) {
					return -1;
				}
				if(o2.getType() == null) {
					return 1;
				}

				return o1.getType().compareTo(o2.getType());
			}
		});

		return images;
		
	}
	
	
	public void saveGeologicalImage(String session_id, GeologicalImage gimg) {
		//Type is the label in selection box, get real URI
		//LOGGER.info(gimg.toString());
		Concept type_concept = sessionManager.getSession(session_id).getConceptForLabel(gimg.getType());
		gimg.setType(type_concept.getIri());
		
		
		//Store img annotation model		
		AnnotationGraphModel data_model = new AnnotationGraphModel();
		
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		
		data_model.loadModelFromFile(data_file_path);
		
		data_model.addTypeTriple(gimg.getIri(), gimg.getType());
		data_model.addLabelTriple(gimg.getIri(), gimg.getLabel());
		data_model.addCommentTriple(gimg.getIri(), gimg.getDescription());
		//location
		data_model.addLiteralTriple(
				gimg.getIri(), 
				URIUtils.getURIForOntologyEntity(URIUtils.HAS_PHYISICAL_LOCATION_PROPERTY_NAME), 
				gimg.getLocation());
		
		try {
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

}
