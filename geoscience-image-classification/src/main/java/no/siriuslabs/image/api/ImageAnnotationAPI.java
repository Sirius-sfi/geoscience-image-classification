package no.siriuslabs.image.api;

import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import no.siriuslabs.image.model.GeologicalImage;
import no.siriuslabs.image.model.ImageType;
import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;
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
	public List<ImageType> getImageTypes(){
		//TODO Query for speciall annotation in ontology
		//Add these type of annotation in projection?
		//Add triple <ClassX isArtefactClas True> as for hidden classes
		return null;
	}
	
	
	public Set<GeologicalImage> getImagesOfGivenTypes(String type){
		return null;
	}
	
	
	
	
	public void printIRI(String iri) {
		System.out.println(iri);
	}
	
	
	
	
	
	
	
	
	
	

	
	

}
