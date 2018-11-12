package no.siriuslabs.image.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import no.siriuslabs.image.model.GeologicalImage;
import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;
import uio.ifi.ontology.toolkit.projection.view.OntologyProjectionAPI;


public class ImageAnnotationAPI extends OntologyProjectionAPI {
		
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
	
	
	//First elemmment top class
	public List<Concept> getImageTypes(String iri){
		//TODO Query for special annotation in ontology	
		List<Concept> imageTypes = new ArrayList<Concept>();
		
		Concept mainArtefact = sessionManager.getSession(iri).getMainArtefactConcept();
		imageTypes.add(mainArtefact);
		
				
		TreeSet<Concept> concepts = sessionManager.getSession(iri).getAllSubClasses(mainArtefact.getIri());
		for (Concept c : concepts.descendingSet()){
			imageTypes.add(c);
		}
		
		
		return imageTypes;
	}
	
	
	public Set<GeologicalImage> getImagesOfGivenTypes(String type){
		//TODO
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	

}
