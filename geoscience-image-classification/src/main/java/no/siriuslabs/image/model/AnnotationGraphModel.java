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
	
	Model model_new;
		
		
		
	public AnnotationGraphModel(){
		
		vf = SimpleValueFactory.getInstance();
		
		//For the new annotations. necessary for incremental reasoning
		model_new=new TreeModel();			
			
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
		model_new.clear();
	}
	
	
	
	public Model getRDFModel(){		
		return model;
	}
	
	
	public Model getRDFModelNewAnnoations(){		
		return model_new;
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
	public void saveNewAnnotationsModel(String tmp_file) throws IOException{
	
		FileOutputStream output_stream = new FileOutputStream(new File(tmp_file));
		Rio.write(model_new, output_stream, RDFFormat.TURTLE); //System.out
		output_stream.close();
		
	}
	
	
	public void addTypeTriple(String subject, String object){
		addObjectTriple(vf.createIRI(subject), RDF.TYPE, vf.createIRI(object));
	}
	
	public void addLabelTriple(String subject, String object){
		addLiteralTriple(vf.createIRI(subject), RDFS.LABEL, vf.createLiteral(object));
	}
	
	public void addCommentTriple(String subject, String object){
		addLiteralTriple(vf.createIRI(subject), RDFS.COMMENT, vf.createLiteral(object));
	}
	
	
	public void addObjectTriple(String subject, String predicate, String object){
		model.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object));
		model_new.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object));
	}
	
	
	public void addObjectTriple(Resource subject, IRI predicate, Value object){
		model.add(subject, predicate, object);
		model_new.add(subject, predicate, object);
	}
	
	public void addLiteralTriple(String subject, String predicate, String object){
		model.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object));
		model_new.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object));
	}
	
	public void addLiteralTriple(String subject, String predicate, String object, String datatype_object){
		model.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object, vf.createIRI(datatype_object)));
		model_new.add(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object, vf.createIRI(datatype_object)));
	}
	
	
	public void addLiteralTriple(Resource subject, IRI predicate, Value object){
		model.add(subject, predicate, object);
		model_new.add(subject, predicate, object);
	}
	
	
	
	public void addTripleStatement(Statement triple) {
		model.add(triple);
		model_new.add(triple);
	}

}
