package no.siriuslabs.image.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uio.ifi.ontology.toolkit.projection.utils.URIUtils;

/**
 * This class manages the RDF annotation graph model
 * @author ejimenez-ruiz
 *
 */
public class AnnotationGraphModel {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationGraphModel.class);
	
	//Factory to create RDF model/triples
	ValueFactory vf;
		
	//RDF Model
	Model model;
	
	//New triples to remove or to add (tmp file for RDFox) 
	Model model_updates;
		
		
		
	public AnnotationGraphModel(){
		
		vf = SimpleValueFactory.getInstance();
		
		//For the new annotations. necessary for incremental reasoning
		model_updates=new TreeModel();			
			
	}
	
	public void createNewModel() {
		// Create a new, empty Model object.
		model = new TreeModel();
			
	}
	
	
	public void loadModelFromFile(String file) {
	

	    try {
	    	File fileObject = new File(file);
	    	FileInputStream input_stream = new FileInputStream(fileObject);
			model = Rio.parse(input_stream, "", RDFFormat.TURTLE);
			input_stream.close();
			
			
	    } catch (RDFParseException | UnsupportedRDFormatException | IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error reading model from '"+ file +"'. Error: " + e.getMessage());
			//e.printStackTrace();
		}
	    
	}
	
	
	public void dispose(){
		model.clear();
		model_updates.clear();
	}
	
	
	
	/** 
	 * Gets main data model
	 * 
	 * @return
	 */
	public Model getRDFModel(){		
		return model;
	}
	
	
	/**
	 * Gets the triples that were either removed or added from/to main models 
	 * @return
	 */
	public Model getRDFModelWithUpdates(){		
		return model_updates;
	}
	
	


	public void saveModel(String file) throws IOException{
		
		//for (Statement st: model){
		//	System.out.println(st.toString());			
		//}
		
		LOGGER.debug("File data model: " + file);

		FileOutputStream output_stream = new FileOutputStream(new File(file));
		Rio.write(model, output_stream, RDFFormat.TURTLE); //System.out
		output_stream.close();
		
	}
	
	
	/**
	 * Saves temporal file with new annotations
	 * @param file
	 * @throws IOException
	 */
	public void saveTmpModelWithUpdates(String tmp_file) throws IOException{
	
		FileOutputStream output_stream = new FileOutputStream(new File(tmp_file));
		Rio.write(model_updates, output_stream, RDFFormat.TURTLE); //System.out
		output_stream.close();
		
	}
	
	
	
	
	public void addTypeTriple(String subject, String object){
		addObjectTriple(vf.createIRI(subject), RDF.TYPE, vf.createIRI(object));
		//We also add direct type
		addObjectTriple(vf.createIRI(subject), vf.createIRI(URIUtils.DIRECT_TYPE), vf.createIRI(object));
	}
	
	
	public void removeTypeTriple(String subject, String object){
		removeObjectTriple(vf.createIRI(subject), RDF.TYPE, vf.createIRI(object));
		//We also remove direct type
		removeObjectTriple(vf.createIRI(subject), vf.createIRI(URIUtils.DIRECT_TYPE), vf.createIRI(object));
	}
	
	
	
	
	public void addLabelTriple(String subject, String object){
		addLiteralTriple(vf.createIRI(subject), RDFS.LABEL, vf.createLiteral(object));
	}
	
	
	public void removeLabelTriple(String subject, String object){
		removeLiteralTriple(vf.createIRI(subject), RDFS.LABEL, vf.createLiteral(object));
	}
	
	
	
	public void addCommentTriple(String subject, String object){
		addLiteralTriple(vf.createIRI(subject), RDFS.COMMENT, vf.createLiteral(object));
	}
	
	
	public void removeCommentTriple(String subject, String object){
		removeLiteralTriple(vf.createIRI(subject), RDFS.COMMENT, vf.createLiteral(object));
	}
	
	
	
	
	
	public void addObjectTriple(String subject, String predicate, String object){
		//model.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object));
		//model_new.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object));
		addObjectTriple(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object));
	}
	
	
	public void addObjectTriple(Resource subject, IRI predicate, Value object){
		model.add(subject, predicate, object);
		model_updates.add(subject, predicate, object);
	}
	
	public void addLiteralTriple(String subject, String predicate, String object){
		//model.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object));
		//model_new.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object));
		addLiteralTriple(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object));
	}
	
	public void addLiteralTriple(String subject, String predicate, String object, String datatype_object){
		//model.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object, vf.createIRI(datatype_object)));
		//model_new.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object, vf.createIRI(datatype_object)));
		addLiteralTriple(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object, vf.createIRI(datatype_object)));
	}
	
	
	public void addLiteralTriple(Resource subject, IRI predicate, Value object){
		model.add(subject, predicate, object);
		model_updates.add(subject, predicate, object);
		
	}
	
	
	
	
	public void removeObjectTriple(String subject, String predicate, String object){
		removeObjectTriple(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object));
	}
	
	
	public void removeObjectTriple(Resource subject, IRI predicate, Value object){
		model.remove(subject, predicate, object);
		model_updates.add(subject, predicate, object); //keeps deleted triples
	}
	
	public void removeLiteralTriple(String subject, String predicate, String object){
		removeLiteralTriple(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object));
	}
	
	public void removeLiteralTriple(String subject, String predicate, String object, String datatype_object){
		removeLiteralTriple(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object, vf.createIRI(datatype_object)));
	}
	
	
	public void removeLiteralTriple(Resource subject, IRI predicate, Value object){
		model.remove(subject, predicate, object);
		model_updates.add(subject, predicate, object); //keeps deleted triples
	}
	
	
	
	
	
	
	
	
	
	public void addTripleStatement(Statement triple) {
		model.add(triple);
		model_updates.add(triple);
	}
	
	
	
	public void removeTripleStatement(Statement triple) {
		
		model.remove(triple);
		
		//mode_new is a tmp object with elements that have been removed or added
		model_updates.add(triple);
	}
	
	
	
	

}
