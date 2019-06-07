package no.siriuslabs.image.api;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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
import no.siriuslabs.image.model.shape.Triangle;
import uio.ifi.ontology.toolkit.constraint.utils.Utility;
import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;
import uio.ifi.ontology.toolkit.projection.model.entities.DataProperty;
import uio.ifi.ontology.toolkit.projection.model.entities.GenericValue;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.LiteralValue;
import uio.ifi.ontology.toolkit.projection.model.entities.ObjectProperty;
import uio.ifi.ontology.toolkit.projection.model.entities.Property;
import uio.ifi.ontology.toolkit.projection.model.triples.DataPropertyTriple;
import uio.ifi.ontology.toolkit.projection.model.triples.ObjectPropertyTriple;
import uio.ifi.ontology.toolkit.projection.model.triples.Triple;
import uio.ifi.ontology.toolkit.projection.model.triples.TypeDefinitionTriple;
import uio.ifi.ontology.toolkit.projection.utils.URIUtils;
import uio.ifi.ontology.toolkit.projection.view.OntologyProjectionAPI;


public class ImageAnnotationAPI extends OntologyProjectionAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyProjectionAPI.class);

	/**
	 * List of predicates that should be hidden in visualization.
	 * Could/should be moved to separate ontology to replace constant.
	 */
	private static final String[] PREDICATES_TO_HIDE_IN_TABLE = {"label", GIC_URIUtils.IS_VISUALLY_REPRESENTED};
	
	//private ValueFactory vf;
	
	
	public ImageAnnotationAPI(RDFoxSessionManager session){
		sessionManager = session;
		
		//vf = SimpleValueFactory.getInstance();
		
		
		
		//TODO: MISSING things
		//1. Retrieval of triples for neighbours (OK) and facets (OK)
		//2. Refined methods for autocompletion
		//2a. Predicates for subject
		//2b. Objects for subject-predicate (facets and neighbourhoods)
		//2b1. Potential allowed values for facets: boolean, list of strings, etc. (slider?)
		//2b2. Expected allowed objects uris
		//3. Remove triples (OK)
		//4. Namespace: filter by namespace in table (ok) and in autocompletion (necessary?)
		//4a. Give to projection manager a set of ontologies or ontology URIs! (ok)
		
		
	}
	
	
	
	protected double getRandomNumber() {
		return Math.random();
	}
	
	
	public int getNumberLoadedOntologies() {
		return sessionManager.getLoadedOntologies().size();
	}
	
	
	public void createNewSession(String iri, String data_file) {
		sessionManager.createNewSession(iri, data_file);
	}
	
	public void createNewSession(String session_id, Set<String> iris, String data_file) {
		sessionManager.createNewSession(session_id, iris, data_file);
	}
	
	
	
	
	/**
	 * Convenience method to retrieve available contributors
	 * @return
	 */
	public Set<String> getAvailableContributors(String session_id){
		
		return sessionManager.getSession(session_id).getObjectsForPredicate(URIUtils.DC_CONTRIBUTOR);
		
	}
	
	
	
	//First element top class
	public List<Concept> getImageTypes(String session_id){
			
		List<Concept> imageTypes = new ArrayList<Concept>();
		
		//TODO Possible alternative: Query for concepts of given namespace?
		//Configure in annotations ontology
		//Query for special annotation in ontology
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
			Set<String> locations = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance.getIri(), GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HAS_PHYISICAL_LOCATION));
			for(String location: locations) {
				geoImage.setLocation(location);
			}
			
			//Add label for type
			//We keep only one
			Set<String> labels = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(geoImage.getClassType(), URIUtils.RDFS_LABEL);
			for(String type_label: labels) {
				geoImage.setTypeLabel(type_label);
			}
			
			
			//Query for creator
			//We keep only one if many
			Set<String> creators = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance.getIri(), URIUtils.DC_CONTRIBUTOR);
			for(String creator: creators) {
				geoImage.setContributor(creator);
			}
			
			
			//Query for datesubmitted
			//We keep only one if many
			Set<String> dates = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance.getIri(), URIUtils.DC_DATESUBMITTED);
			for(String date: dates) {
				geoImage.setDateSubmission(date);
			}
			
			
			
			//Source/provenance
			Set<String> sources = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance.getIri(), GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HAS_PROVENANCE));
			for(String source: sources) {
				geoImage.setSource(source);
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
		
		
		//1. Get Points (object) for a shape
		Set<String> point_uris = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(shape_uri, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HASPOINT));
		
		//We initialize with the proper size
		WPointF[] points_vector = new WPointF[point_uris.size()];
		//System.out.println(shape_uri + " " +point_uris.size());
		
		//List<WPointF> points = new ArrayList<WPointF>();
		
		System.out.println("Getting points for shape: " + shape_uri);
		
		for (String point_uri : point_uris) {
			
			System.out.println("Getting point details: " + point_uri);
			
			WPointF point = new WPointF();
			
			//coordinate x
			Set<String> values = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(point_uri, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HASX));
			for (String value: values) {//only one expected
				//System.out.println("\tHASX "+value);
				point.setX(Double.valueOf(value));
			}
			
			//coordinate y
			values = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(point_uri, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HASY));
			for (String value: values) {//only one expected
				//System.out.println("\tHASY "+value);
				point.setY(Double.valueOf(value));
			}
			
			values = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(point_uri, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HASPOINTORDER));
			
			//Typically only one
			int order_point=0;
			for (String value : values) {
				//System.out.println("\tORDER"+value);
				order_point = Math.round(Float.valueOf(value));//In case it contains decimals (e.g. 0.0000000). We detected this behaviour in RRDFox
				//System.out.println(order_point + " " + value);
				
				//Safety check but issue has been solved about duplicated (same URI) pints with different order. 
				if (order_point>point_uris.size()-1) { //safety check
					order_point = point_uris.size()-1;
				}
				
			}
			
			//order
			//System.out.println(order_point +  "  " + point.getX() + "  " + point.getY());
			points_vector[order_point] = point;
			
		}
		
		
		//Safety check to avoid null points. Issue solved
		List<WPointF> list_points = Arrays.asList(points_vector);
		Set<Integer> toRemove = new HashSet<Integer>();
		
		for (int i=0; i<list_points.size(); i++) {
			if (list_points.get(i)==null)
				toRemove.add(i);
		}
		for (int i : toRemove)
			list_points.remove(i);
		
		 
		return list_points;
		
	}
	
	
	
	
	public Set<AbstractShape> getSelectionShapesForImage(String session_id, String image_uri){
		
		
		Set<AbstractShape> shapes = new HashSet<AbstractShape>();
		
		
		//1. Get shapes (subject) visually represented in image
		
		Set<String> shape_uris = sessionManager.getSession(session_id).getSubjectsForObjectPredicate(GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IS_SELECTION_OF), image_uri);
		
		//Utility.println(shape_uris.size() + " shapes for " +image_uri);
		
		
		for (String uri_shape : shape_uris) {
			
			
			AbstractShape shape;
			
			
			//Get semantic type
			String type = sessionManager.getSession(session_id).getMostConcreteTypeForInstance(uri_shape);
			if (type.equals(""))
				type = URIUtils.OWL_THING;
			
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
	
	
	
	
	/**
	 * Retrieves ALL annotations associated to an image: annotated objects and types, relationships among objects
	 * and facets associated to objects
	 * @return
	 */
	public Set<Triple> getAllImageAnnotations(String session_id, String image_uri){
	
		Set<Triple> triples = new HashSet<Triple>();
		
		
		//Get elements visually represented in image: reasoning important as some object may be represented in a selection and the selection in the image
		//Shapes belong to image but a different property was used
		Set<String> object_uris = sessionManager.getSession(session_id).getSubjectsForObjectPredicate(GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED), image_uri);
		
		
		//System.out.println("Objects in image: " + object_uris);
		
				
		for (String object_uri : object_uris) {
			
			
			//System.out.println("\t" + object_uri);
			
		
			
			//Get Semantic type
			String sem_type = sessionManager.getSession(session_id).getMostConcreteTypeForInstance(object_uri);
			
			//Create instance object
			Instance object_instance = sessionManager.getSession(session_id).createInstance(object_uri, sem_type);
			
			
			//Types
			triples.addAll(getObjectsAndTypeAnnotationsForImage(session_id, object_instance));
			
			//Shapes
			triples.addAll(getObjectsAndVisualRepresentationAnnotationsForImage(session_id, object_instance, image_uri));
			
			
			//Triples: Relationships
			triples.addAll(getObjectRelationships(session_id, object_instance));
			
			//Triples: Facets
			triples.addAll(getFacets(session_id, object_instance));
			
			
			
		}
		
		return triples;
		
	}
	
	
	
	/**
	 * Retrieves annotated objects and their types associated to an image. 
	 * Triples like:  well_1 rdf:type Well
	 * @param session_id
	 * @param image_uri
	 * @return
	 */
	protected Set<Triple> getObjectsAndTypeAnnotationsForImage(String session_id, Instance instance_object){
	
		Set<Triple> triples = new HashSet<Triple>();
		
			
	
		//Concept type_concept = sessionManager.getSession(session_id).createConcept(sem_type);
			
			
		if (instance_object.getClassType().equals(URIUtils.OWL_THING))
			triples.add(new TypeDefinitionTriple(
					instance_object,
					new Concept(URIUtils.OWL_THING)));
		
		else
			triples.add(new TypeDefinitionTriple(
					instance_object,
					sessionManager.getSession(session_id).createConcept(instance_object.getClassType())));
			
		
		return triples;
		
	}
	
	
	/**
	 * Retrieves annotated objects and in which selection it appears (default: the whole image)
	 * Triples like: well_1 isVisuallyRepresentedIn circle_2
	 * 
	 * @param session_id
	 * @param image_uri
	 * @return
	 */
	protected Set<Triple> getObjectsAndVisualRepresentationAnnotationsForImage(String session_id, Instance instance_object, String image_uri){
		
		Set<Triple> triples = new HashSet<Triple>();
		
		String predicate = GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED);
			
		//TODO Can an object be attached to more than one selection? (Transitivity and containment of selections not allowed yet)
		Set<String> selection_uris = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance_object.getIri(), predicate, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IMAGE_SELECTION));
		
		//if empty then object was not attached to image selection
		if (selection_uris.isEmpty()) 
				
			triples.add(new ObjectPropertyTriple(
					instance_object,
					sessionManager.getSession(session_id).createObjectPropery(predicate),
					new Instance(image_uri)));
			
		else {
			for (String selection_uri : selection_uris) { //only one expected
				triples.add(new ObjectPropertyTriple(
						instance_object,
						sessionManager.getSession(session_id).createObjectPropery(predicate),
						new Instance(selection_uri)));
			}
		}							
			
		return triples;
		
		
	}
		

	/**
	 * Returns object property triples
	 * @param session_id
	 * @param instance_object
	 * @return
	 */
	public Set<ObjectPropertyTriple> getObjectRelationships(String session_id, Instance instance_object){
		
		Set<ObjectPropertyTriple> triples = new HashSet<ObjectPropertyTriple>();
		
		//TODO Retrieve triples where object is of type Thing (is this inferred? YES) (and/or predicate of type object property)
		
		
		Map<String, Set<String>> object_relationhips_map = sessionManager.getSession(session_id).getObjectRelationhipsForSubject(instance_object.getIri());
		
		
		for (String p : object_relationhips_map.keySet()){
			
			if (p.equals(GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED))
				|| p.equals(URIUtils.OWLTopObjectProperty))
				continue;
			
			for (String o: object_relationhips_map.get(p)) {
			triples.add(new ObjectPropertyTriple(
					instance_object,
					sessionManager.getSession(session_id).createObjectPropery(p),
					sessionManager.getSession(session_id).createInstance(o)));
			}
		}
		
		
		return triples;
		
	}
	
	
	
	/**
	 * Returns data property triple
	 * @param session_id
	 * @param instance_object
	 * @return
	 */
	public Set<DataPropertyTriple> getFacets(String session_id, Instance instance_object){
		
		Set<DataPropertyTriple> triples = new HashSet<DataPropertyTriple>();
		
		//TODO Retrieve triples where object is a literal (and/or predicate of type data property)
		
		Map<String, Set<String>> data_relationhips_map = sessionManager.getSession(session_id).getDataRelationhipsForSubject(instance_object.getIri());
		
		
		for (String p : data_relationhips_map.keySet()){
			for (String literal: data_relationhips_map.get(p)) {
			triples.add(new DataPropertyTriple(
					instance_object,
					sessionManager.getSession(session_id).createDataPropery(p),
					new LiteralValue(literal)));
			}
		}
		
		
		return triples;
		
	}
	
	
	
	
	/**
	 * Possible types for an identified object in the image
	 * @param session_id
	 * @return
	 */
	public TreeSet<Concept> getOntologyConcepts(String session_id){
		
		//TODO Filter by namespace: getNamespaceToHideInVisualization()
		return sessionManager.getSession(session_id).getCoreConcepts(getNamespaceToHideInVisualization());
	
	}
	
	
	/**
	 * Possible values for subject and objects
	 * @param session_id
	 * @return
	 */
	public TreeSet<Instance> getIndividuals(String session_id){
		
		return sessionManager.getSession(session_id).getInstances();
	}
	
	
	/**
	 * Possible values for subject and objects
	 * @param session_id
	 * @return
	 */
	public TreeSet<Instance> getIndividualsByType(String session_id, String type){
		
		return sessionManager.getSession(session_id).getInstancesForType(type);
	}
	
	
	/**
	 * Possible values for subject
	 * @param session_id
	 * @return
	 */
	public TreeSet<Instance> getSubjectsResources(String session_id){
		return getIndividuals(session_id);
	}
	
	
	/**
	 * Possible values for objects of type instance/resource
	 * @param session_id
	 * @return
	 */
	public TreeSet<Instance> getObjectResources(String session_id){
		return getIndividuals(session_id);
	}
	
	
	
	
	
	
	public TreeSet<Property> getPredicates(String session_id){
		TreeSet<Property> predicates = new TreeSet<Property>();
		
		//Avoid null or return empty set
		predicates.addAll(getDataPredicates(session_id));
		predicates.addAll(getObjectPredicates(session_id));
		
		return predicates;
		
	}
	
	
	public TreeSet<Property> getAllowedPredicatesForSubject(String session_id, String subject_iri){
		TreeSet<Property> predicates = new TreeSet<Property>();

		final TreeSet<DataProperty> dataPredicatesForSubject = getAllowedDataPredicatesForSubject(session_id, subject_iri);
		if(dataPredicatesForSubject != null) {
			predicates.addAll(dataPredicatesForSubject);
		}

		final TreeSet<ObjectProperty> objectPredicatesForSubject = getAllowedObjectPredicatesForSubject(session_id, subject_iri);
		if(objectPredicatesForSubject != null) {
			predicates.addAll(objectPredicatesForSubject);
		}

		return predicates;
		
	}
	
	
	
	
	
	
	
	public TreeSet<DataProperty> getDataPredicates(String session_id){
		return sessionManager.getSession(session_id).getDataPredicates();
	}
	
	
	public TreeSet<ObjectProperty> getObjectPredicates(String session_id){
		return sessionManager.getSession(session_id).getObjectPredicates();
	}
	
	
	/**
	 * Gets data properties that are expected to be used by subject
	 * @param session_id
	 * @param subject_iri
	 * @return
	 */
	public TreeSet<DataProperty> getAllowedDataPredicatesForSubject(String session_id, String subject_iri){
		
		//Similar to getfacets for the type of subject
		return sessionManager.getSession(session_id).getAllowedDataPredicatesForSubject(subject_iri);
		
	}
	
	
	/**
	 * Gets object properties that are expected to be used by subject
	 * @param session_id
	 * @param subject_iri
	 * @return
	 */
	public TreeSet<ObjectProperty> getAllowedObjectPredicatesForSubject(String session_id, String subject_iri){
		//Similar to  getneighbours for the type of subject
		return sessionManager.getSession(session_id).getAllowedObjectPredicatesForSubject(subject_iri);
	}

	
	
	
	/**
	 * Get values (instances or litearals) that are expected for the combination subject-predicate
	 * @param session_id
	 * @param subject_iri
	 * @param predicate_iri
	 * @return
	 */
	public TreeSet<Instance> getAllowedObjectValuesForSubjectPredicate(String session_id, String subject_iri, String predicate_iri){
		//There is a method in session: getObjectsforSubjectPredicate
		
		//TODO Include allowed literal values?
		return sessionManager.getSession(session_id).getAllowedObjectInstancesForSubjectPredicate(subject_iri, predicate_iri);
	
	}
	
	
	

	/**
	 * Get allowed values (object) that are expected for the combination subject-predicate
	 * @param session_id
	 * @param subject_iri
	 * @param predicate_iri
	 * @return
	 */
	public TreeSet<String> getAllowedValues(String session_id, String subject_iri, String predicate_iri){
		return null;
		
		//TODO return literals? Only if there is a range of possible values: e.g. boolean, list of companies, etc. Check projection
		//Merged with previous method
		
		
	}
	
	
	
	/**
	 * Saves annotations/triples from interface. 
	 */
	public void saveAnnotations(String session_id, Set<Triple> triples){
		
		//Store img annotation model		
		AnnotationGraphModel data_model = new AnnotationGraphModel();
		
		//Load model
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
		
		//Add triples
		addTriplesForAnnotations(data_model, triples);
		
		//Save new triples
		saveDataModel(data_model, session_id); //by default is addition
		
		
	}
	
	
	
	/**
	 * Removed annotations/tripels from interface. 
	 */
	public void removeAnnotations(String session_id, Set<Triple> triples){
		
		//Store img annotation model		
		AnnotationGraphModel data_model = new AnnotationGraphModel();
		
		//Load model
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
		
		removeTriplesForAnnotations(data_model, triples);
		
		//Update model and rdfox model
		//saveDataModel(data_model, session_id, UpdateType.ScheduleForDeletion);
		saveDataModelAndSyncDeletion(data_model, session_id);
		
		
	}
	
	
	protected void addTriplesForAnnotations(AnnotationGraphModel data_model, Set<Triple> triples) {
		
		//Add triples to model
		for (Triple triple : triples) {
			
			//is_visually_represented triple already added (internally) when associating object to selection shape or to image
			
			if (triple.isTypeDefinitionTriple())
				data_model.addTypeTriple(
						triple.getSubject().getIri(), triple.asTypeDefinitionTriple().getObject().getIri());
			
			else if(triple.isDataPropertyTriple())
				data_model.addLiteralTriple(
						triple.getSubject().getIri(), triple.asDataPropertyTriple().getPredicate().getIri(), 
						triple.asDataPropertyTriple().getObject().getValue(), triple.asDataPropertyTriple().getObject().getDatatypeString());
			
			else if (triple.isObjectPropertyTriple())
				data_model.addObjectTriple(
						triple.getSubject().getIri(), triple.asObjectPropertyTriple().getPredicate().getIri(), triple.asObjectPropertyTriple().getObject().getIri());
			
		
		}

	}
	
	
	protected void removeTriplesForAnnotations(AnnotationGraphModel data_model, Set<Triple> triples) {
	
		//Remove triples from main model (also keep triples to remove from Rdfox)
		for (Triple triple : triples) {
			
			if (triple.isTypeDefinitionTriple())
				data_model.removeTypeTriple(
						triple.getSubject().getIri(), triple.asTypeDefinitionTriple().getObject().getIri());
			
			else if(triple.isDataPropertyTriple())
				data_model.removeLiteralTriple(
						triple.getSubject().getIri(), triple.asDataPropertyTriple().getPredicate().getIri(), 
						triple.asDataPropertyTriple().getObject().getValue(), triple.asDataPropertyTriple().getObject().getDatatypeString());	
			
			
			else if (triple.isObjectPropertyTriple())
				data_model.removeObjectTriple(
						triple.getSubject().getIri(), triple.asObjectPropertyTriple().getPredicate().getIri(), triple.asObjectPropertyTriple().getObject().getIri());
			
		}

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
				GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HAS_PHYISICAL_LOCATION), 
				gimg.getLocation());
		
		
		data_model.addLiteralTriple(
				gimg.getIri(), 
				URIUtils.DC_CONTRIBUTOR, 
				gimg.getContributor());
		
		
		data_model.addLiteralTriple(
				gimg.getIri(), 
				URIUtils.DC_DATESUBMITTED, 
				gimg.getDateSubmission());
		
		
		if (gimg.getSource()!=null && !gimg.getSource().isEmpty()) {
			data_model.addLiteralTriple(
					gimg.getIri(), 
					GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HAS_PROVENANCE), 
					gimg.getSource());	
		}
		
		
		//Save new triples
		saveDataModel(data_model, session_id);
		
	}
	
	
	
	
	public String getNewSelectionShapeURI() {
		
		//Create URI for shape
		return getNewResourceURI(GIC_URIUtils.SHAPE_OBJECT_PREFIX);
		
	}
	
	
	
	public String getNewURIForObject(String typeName, String labelName) {
		
		//Create URI for object
		//e.g.: typeName=Well, labelName=well 123
		//return getNewResourceURI(typeName.toLowerCase() + "-" + labelName.replaceAll("\\s+","").toLowerCase());
		return getNewResourceURI(typeName.toLowerCase());
	}
	
	
	
	
	
		
	
	
	
	
	
	
	
	public Set<Triple> saveNewShapeAndObject(String session_id, String image_uri, AbstractShape shape, String type_object_uri, String name_object_or_uri){
		return saveNewShapeAndObject(session_id, image_uri, shape, getNewSelectionShapeURI(), type_object_uri, name_object_or_uri);
	}
	
	
	
	
	
	public Set<Triple> saveNewShapeAndObject(String session_id, String image_uri, AbstractShape shape, String uri_shape, String type_object_uri, String name_object_or_uri){
		
		
		//Load model
		AnnotationGraphModel data_model = new AnnotationGraphModel();
		
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
		
		//Add new triples to model
		//About the sahpe
		String shape_type = addNewShapeTriples(data_model, image_uri, shape, uri_shape);
		//About the object being visually represented in shape
		String uri_object = addNewObjectTriples(data_model, uri_shape, type_object_uri, name_object_or_uri);
	
		
		//Save/store model
		saveDataModel(data_model, session_id);
		
		
		
		
		//Retrieve triples from store with necessary labelling/visual data
		Set<Triple> triplesForInterface = new HashSet<Triple>();
		
		
		Instance instance = sessionManager.getSession(session_id).createInstance(uri_object, type_object_uri);
		
		triplesForInterface.add(
				new TypeDefinitionTriple(
						instance,
						sessionManager.getSession(session_id).createConcept(type_object_uri)
						));
		
		
		triplesForInterface.add(
				new ObjectPropertyTriple(
						instance,
						sessionManager.getSession(session_id).createObjectPropery(
								GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED)),
						sessionManager.getSession(session_id).createInstance(uri_shape, shape_type)
						));
		
		
		DataProperty dp = new DataProperty(URIUtils.RDFS_LABEL);
		dp.setLabel("rds:label");
		triplesForInterface.add(
				new DataPropertyTriple(
						instance,
						dp,
						new LiteralValue(name_object_or_uri)
						));
		
		
		
		return triplesForInterface;
		
	}
	
	
	
	public String saveNewObject(String session_id, String shape_uri, String type_object_uri, String name_object) {
		
		
		//Load model
		AnnotationGraphModel data_model = new AnnotationGraphModel();
		
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
	
		//Add triples to model
		String uri_object = addNewObjectTriples(data_model, shape_uri, type_object_uri, name_object);
		
				
		//Save new triples
		saveDataModel(data_model, session_id);
				
		return uri_object;
		
	}
	
	
	
	protected String addNewObjectTriples(AnnotationGraphModel data_model, String shape_uri, String type_object_uri, String name_object_or_uri) {

		
		String uri_object;
		
		
		//Already given a URI for the object
		if (URIUtils.isValidURI(name_object_or_uri)) {
			uri_object = name_object_or_uri;
		}
		//If given a name, then create URI and suitable triples: type and label
		else {
		
			uri_object = getNewURIForObject(URIUtils.getEntityLabelFromURI(type_object_uri), name_object_or_uri);
			
			//Instance instance = new Instance(uri_object);
			//instance.setLabel(name_object);
			//instance.setClassType(type_object_uri);
			//Concept type = new Concept(type_object_uri);
			//new TypeDefinitionTriple(instance, type);
			
			
			//Type object
			data_model.addTypeTriple(uri_object, type_object_uri);
			
			//Label_name
			data_model.addLabelTriple(uri_object, name_object_or_uri);
		
		}
			
		//Visually represented
		data_model.addObjectTriple(uri_object, 
				GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED),
				shape_uri);
		
		
		
		
		return uri_object;
		
	}
	
	
	
	
	
	
	public String saveNewSelectionShape(String session_id, String image_uri, AbstractShape shape) {
		
		return saveNewSelectionShape(session_id, image_uri, shape, getNewSelectionShapeURI());
		
	}
	
	
	public String saveNewSelectionShape(String session_id, String image_uri, AbstractShape shape, String uri_shape) {
	
		//Store img annotation model		
		AnnotationGraphModel data_model = new AnnotationGraphModel();
				
		
		//Load model
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
		
		
		//Add triples to model
		addNewShapeTriples(data_model, image_uri, shape, uri_shape);
		
		
		//Save new triples
		saveDataModel(data_model, session_id);
		
		return uri_shape;
		
		
		
	}
	
	
	
	protected String addNewShapeTriples(AnnotationGraphModel data_model, String image_uri, AbstractShape shape, String uri_shape) {
		//Create URI for shape
		//String uri_shape = getNewResourceURI("shape");
		
		//Assign URI type depending on the shapeType: Trianle, Circle...
		String shape_type = GIC_URIUtils.getURIForAnnotationOntologyEntity(
				StringUtils.capitalize(shape.getShapeType().toString().toLowerCase()));
		
		data_model.addTypeTriple(uri_shape, shape_type);
		
		
		//Link image with shape/selection
		data_model.addObjectTriple(uri_shape, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IS_SELECTION_OF), image_uri);
		
		//Add points to shape
		String point_uri;
		int i = 0;
		for (WPointF point : shape.getPoints()){
			point_uri = getNewResourceURI("point");
			
			//Type Point
			data_model.addTypeTriple(point_uri, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.POINT));
			
			//link between shape and points
			data_model.addObjectTriple(uri_shape, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HASPOINT), point_uri);
			
			//coordinates
			data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HASX), String.valueOf(point.getX()), URIUtils.XSD_DOUBLE);
			data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HASY), String.valueOf(point.getY()), URIUtils.XSD_DOUBLE);
			
			//For circles and polygons with 5 or more points the order of points matters
			data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HASPOINTORDER), String.valueOf(i), URIUtils.XSD_STRING);
			
			//System.out.println(i + " " + point.getX() + "  " + point.getY());
			i++;
		
			
			
		}
		
		return shape_type;
		
	}
	
	
	
	private Set<String> visited_uris = new HashSet<String>();
	
	
	
	
	/**
	 * Removes image and all the objects contained in it. Recursion level 3 
	 * @param session_id
	 * @param object_uri
	 */
	public void removeImage(String session_id, String image_uri) {
		removeElement(session_id, image_uri, 3);
	}
	
	
	
	/**
	 * Removes objects in image. Recursion level 1 as we do not remove referred/referring objects 
	 * @param session_id
	 * @param object_uri
	 */
	public void removeObjectInImage(String session_id, String object_uri) {
		removeElement(session_id, object_uri, 1);
	}

	
	/**
	 * We remove a shape and the elements related to shape till level 2
	 * e.g. for points and for objects in shape, but we do not remove other objects in other shapes (just the link/triple)
	 * @param session_id
	 * @param shape_uri
	 */
	public void removeShape(String session_id, String shape_uri) {
		removeElement(session_id, shape_uri, 2);
	}

	/**
	 * Removes an element, its types, the triples pointing to that elements and the triples with that element as subject
	 * @param session_id
	 * @param element_uri
	 * @param recursion_level How many levels of referencing objets we want to remove
	 */
	public void removeElement(String session_id, String element_uri, int recursion_level) {
		
		visited_uris.clear();
						
		//Load model to change
		AnnotationGraphModel data_model = new AnnotationGraphModel();
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
		
		//Removes directly from datamodel		
		removeRelatedTriplesForElement(session_id, data_model, element_uri, recursion_level); 
		
		//Save updated models: physically and on RDFox
		//saveDataModel(data_model, session_id, UpdateType.ScheduleForDeletion);
		saveDataModelAndSyncDeletion(data_model, session_id);
				
		
		
	}
	
	
	
	/**
	 * Removes from datamodel the set of related triples
	 * @param session_id
	 * @param data_model
	 * @param element_uri
	 * @param recursion_level
	 */
	protected void removeRelatedTriplesForElement(String session_id, AnnotationGraphModel data_model, String element_uri, int recursion_level) {
		
		visited_uris.add(element_uri);
		
				
		if (recursion_level==0)
			return;
				
		
		//ELEMENT AS SUBJECT		
		//System.out.println("1: "+ sessionManager.getSession(session_id).getAllRelationhipsForSubject(element_uri));
		Map<String, Set<GenericValue>> pred2object = sessionManager.getSession(session_id).getAllRelationhipsForSubject(element_uri);
		
		for (String predicate : pred2object.keySet()) {
			
			if (predicate.equals(URIUtils.OWL_SAMEAS) || predicate.equals(URIUtils.OWLTopDataProperty) || predicate.equals(URIUtils.OWLTopObjectProperty))
				continue;
			
			//For object properties and others we only get the value from GenericValue
			
			//One level of recursion: e.g. for points and for objects in shape, but we do not remove other objects in other shapes (just the link/triple)
			if (sessionManager.getSession(session_id).isPredicateAnObjectProperty(predicate)) {
				
				for (GenericValue value_uri : pred2object.get(predicate)) {
					data_model.removeObjectTriple(element_uri, predicate, value_uri.getValue());
					
					//exception for recursion (case of shapes, we do not want to remove the image) 
					if (predicate.equals(GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.IS_SELECTION_OF)))  //http://no.sirius.ontology/ann#isSelectionOf
						continue;
					
					//Recursion if new element
					if (!visited_uris.contains(value_uri.getValue())) {						
						removeRelatedTriplesForElement(session_id, data_model, value_uri.getValue(), recursion_level-1);
					}
					
				}
				
				
				
			}
			//Types are special, we do not want to remove the concept with recursion
			else if (predicate.equals(URIUtils.RDF_TYPE) || predicate.equals(URIUtils.DIRECT_TYPE)) {
				
				for (GenericValue value_type : pred2object.get(predicate)) {
					data_model.removeObjectTriple(element_uri, predicate, value_type.getValue());
				}
				
			}
			else { //Check if dataproperty to avoid top property and sameas
							
				
				for (GenericValue value : pred2object.get(predicate)) {		
					//System.out.println("Removing: " + predicate + " " + value.getValue() +" " + value.getType());
					data_model.removeLiteralTriple(element_uri, predicate, value.getValue(), value.getType()); 
				}
			
				
			}
			
		
			
		}
		
		
		//ELEMENT AS OBJECT
		//System.out.println("2: "+ sessionManager.getSession(session_id).getAllRelationhipsForObject(element_uri));
		Map<String, Set<String>> pred2subject = sessionManager.getSession(session_id).getAllRelationhipsForObject(element_uri);
		
		for (String predicate : pred2subject.keySet()) {
			
			//One level of recursion: e.g. for points and for objects in shape, but we do not remove other objects in other shapes (just the link/triple)
			if (sessionManager.getSession(session_id).isPredicateAnObjectProperty(predicate)) { //safety check, other cases does not apply as now the element is in the object position
				
				for (String value_uri : pred2subject.get(predicate)) {
					data_model.removeObjectTriple(value_uri, predicate, element_uri);
					
					//exception for recursion (case of shapes, we do not want to remove the image)
					if (predicate.equals(GIC_URIUtils.getURIForAnnotationOntologyEntity(GIC_URIUtils.HAS_SELECTION)))  //http://no.sirius.ontology/ann#hasSelection
						continue;
					
					//Recursion if new element
					if (!visited_uris.contains(value_uri)) {						
						removeRelatedTriplesForElement(session_id, data_model, value_uri, recursion_level-1);
					}
					
				}
				
				
				
			}
			
			
		}
		
		
		
		
		
		
	}
	
	
	
	/**
	 * Saves models and clears structures. Default update strategy: addition
	 * @param data_model
	 * @param session_id
	 */
	private void saveDataModel(AnnotationGraphModel data_model, String session_id) {
		//saveDataModel(data_model, session_id, UpdateType.ScheduleForAddition);
		saveDataModelAndSyncAddition(data_model, session_id);
	}

		
	
	
	/**
	 * Saves models and clears structures
	 * @param data_model
	 * @param session_id
	 * @param importType To add or to remove triple isn RDFox
	 */
	private void saveDataModelAndSyncDeletion(AnnotationGraphModel data_model, String session_id) {
		
		
		try {
			
			String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
			data_model.saveModel(data_file_path);
			
			//Save tmp file with new added/removed annotations and perform incremental reasoning in RDFox
			String tmp_file = Utility.tmp_directory + "tmp_file_annotations.ttl";
			data_model.saveTmpModelWithOnlyUpdates(tmp_file);
			
			//Sync RDFox reasoner
			sessionManager.getSession(session_id).performMaterializationAdditionalData(
					tmp_file, true, sessionManager.getSession(session_id).getDeletionUpdateType());//incremental reasoning
			
			//dispose/clear models
			data_model.dispose();
			
			
		} catch (Exception e) {
			LOGGER.error("Error storing the annotation model. Error: "+e.getMessage());
		}
		
	}
	
	
	
	/**
	 * Saves models and clears structures
	 * @param data_model
	 * @param session_id
	 * @param importType To add or to remove triple isn RDFox
	 */
	private void saveDataModelAndSyncAddition(AnnotationGraphModel data_model, String session_id) {
		
		
		try {
			
			String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
			data_model.saveModel(data_file_path);
			
			//Save tmp file with new added/removed annotations and perform incremental reasoning in RDFox
			String tmp_file = Utility.tmp_directory + "tmp_file_annotations.ttl";
			data_model.saveTmpModelWithOnlyUpdates(tmp_file);
			//Sync RDFox reasoner
			sessionManager.getSession(session_id).performMaterializationAdditionalData(
					tmp_file, true, sessionManager.getSession(session_id).getAdditionUpdateType());//incremental reasoning
			
			//dispose/clear models
			data_model.dispose();
			
			
		} catch (Exception e) {
			LOGGER.error("Error storing the annotation model. Error: "+e.getMessage());
		}
		
	}
	
	
	
	
	
	private String getNewResourceURI(String base_name) {
		Random randomNum = new Random();
		int random = 1 + randomNum.nextInt(10000); // + randomNum.nextInt(1000);
		String new_uri = GIC_URIUtils.getURIForResource(base_name + "-"+ random + Calendar.getInstance().getTimeInMillis());
		
		try {
			TimeUnit.MILLISECONDS.sleep(1); //to avoid same calendar instance + same random 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return  new_uri;
	}
	
	public List<String> getPredicatesToHideInVisualization() {
		return Arrays.asList(PREDICATES_TO_HIDE_IN_TABLE);
	}
	
	public String getNamespaceToHideInVisualization() {
		return GIC_URIUtils.BASE_URI_ANNOTATIONS + "#";
	}
	

	
	

}
