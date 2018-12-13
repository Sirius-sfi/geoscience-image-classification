package no.siriuslabs.image.api;

import java.util.ArrayList;
import java.util.Arrays;
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
import no.siriuslabs.image.model.shape.Triangle;
import no.siriuslabs.image.model.triples.DataPropertyTriple;
import no.siriuslabs.image.model.triples.ObjectPropertyTriple;
import no.siriuslabs.image.model.triples.Triple;
import no.siriuslabs.image.model.triples.TypeDefinitionTriple;
import uio.ifi.ontology.toolkit.constraint.utils.Utility;
import uio.ifi.ontology.toolkit.projection.controller.triplestore.RDFoxSessionManager;
import uio.ifi.ontology.toolkit.projection.model.entities.Concept;
import uio.ifi.ontology.toolkit.projection.model.entities.DataProperty;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.LiteralValue;
import uio.ifi.ontology.toolkit.projection.model.entities.ObjectProperty;
import uio.ifi.ontology.toolkit.projection.model.entities.Property;
import uio.ifi.ontology.toolkit.projection.utils.URIUtils;
import uio.ifi.ontology.toolkit.projection.view.OntologyProjectionAPI;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;



public class ImageAnnotationAPI extends OntologyProjectionAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyProjectionAPI.class);
	
	//private ValueFactory vf;
	
	
	public ImageAnnotationAPI(RDFoxSessionManager session){
		sessionManager = session;
		
		//vf = SimpleValueFactory.getInstance();
		
		
		
		//TODO: MISSING things
		//1. Retrieval of triples for neighbours and facets
		//2. Refined methods for autocompletion
		//2a. Predicates for subject
		//2b. Objects for subject-predicate (facets and neighbourhoods)
		//2b1. Potential allowed values for facets: boolean, list of strings, etc. (slider?)
		//2b2. Expected allowed objects uris
		
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
	
	
	//First element top class
	public List<Concept> getImageTypes(String session_id){
			
		List<Concept> imageTypes = new ArrayList<Concept>();
		
		//TODO Possible alternative: Query for concepts of given namespace?
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
			Set<String> locations = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance.getIri(), GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HAS_PHYISICAL_LOCATION));
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
		
		
		//1. Get Points (object) for a shape
		Set<String> point_uris = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(shape_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASPOINT));
		
		//We initialize with the proper size
		WPointF[] points_vector = new WPointF[point_uris.size()];
		//List<WPointF> points = new ArrayList<WPointF>();
		
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
			
			values = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASPOINTORDER));
			
			//Typically only one
			int order_point=0;
			for (String value : values)
				order_point = Integer.valueOf(value);
			
			//order
			//System.out.println(order_point +  "  " + point.getX() + "  " + point.getY());
			points_vector[order_point] = point;
			
			
		}
		
		
		
		return Arrays.asList(points_vector);
		
	}
	
	
	
	
	public Set<AbstractShape> getSelectionShapesForImage(String session_id, String image_uri){
		
		
		Set<AbstractShape> shapes = new HashSet<AbstractShape>();
		
		
		//1. Get shapes (subject) visually represented in image
		Set<String> shape_uris = sessionManager.getSession(session_id).getSubjectsForObjectPredicate(GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IS_SELECTION_OF), image_uri);
		
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
	
	
	
	
	/**
	 * Retrieves ALL annotations associated to an image: annotated bjects and types, relationships among objects
	 * and facets associated to objects
	 * @return
	 */
	public Set<Triple> getAllImageAnnotations(String session_id, String image_uri){
	
		Set<Triple> triples = new HashSet<Triple>();
		
		
		//Get elements visually represented in image: reasoning important as some object may be represented in a selection and the selection in the image
		Set<String> object_uris = sessionManager.getSession(session_id).getSubjectsForObjectPredicate(GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED), image_uri);
				
				
		for (String object_uri : object_uris) {
		
			
			//Get Semantic type
			String sem_type = sessionManager.getSession(session_id).getMostConcreteTypeForInstance(object_uri);
			
			//Create instance object
			Instance object_instance = sessionManager.getSession(session_id).createInstance(object_uri, sem_type);
			
			
			//Types
			triples.addAll(getObjectsAndTypeAnnotationsForImage(session_id, object_instance));
			
			//Shapes
			triples.addAll(getObjectsAndVisualRepresentationAnnotationsForImage(session_id, object_instance, image_uri));
			
			
			//TODO: GET triples for relationships and facets
		
			
			
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
			
			
		if (instance_object.getClassType().equals(URIUtils.OWLTHING))
			triples.add(new TypeDefinitionTriple(
					instance_object,
					new Concept(URIUtils.OWLTHING)));
		
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
		
		String predicate = GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED);
			
		//TODO Can an object be attached to more than one selection? (Transitivity and containment of selections not allowed yet)
		Set<String> selection_uris = sessionManager.getSession(session_id).getObjectsForSubjectPredicate(instance_object.getIri(), predicate, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IMAGE_SELECTION));
		
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
		

	public Set<Triple> getRelationshipsAmongObjectsForImage(String session_id, String image_uri){
		
		Set<Triple> triples = new HashSet<Triple>();
		
		//TODO Retrieve triples where object is of type Thing (is this inferred? YES) (and/or predicate of type object property)
		
		return triples;
		
	}
	
	
	
	public Set<Triple> getObjectFacetsForImage(String session_id, String image_uri){
		
		Set<Triple> triples = new HashSet<Triple>();
		
		//TODO Retrieve triples where object is a literal (and/or predicate of type data property)
		
		return triples;
		
	}
	
	
	
	
	/**
	 * Possible types for an identified object in the image
	 * @param session_id
	 * @return
	 */
	public TreeSet<Concept> getOntologyConcepts(String session_id){
		
		return sessionManager.getSession(session_id).getCoreConcepts();
	
	}
	
	
	/**
	 * Possible values for subject and objects
	 * @param session_id
	 * @return
	 */
	public TreeSet<Instance> getIndividuals(String session_id){
		
		//TODO we may want to filter by type
		return sessionManager.getSession(session_id).getInstances();
	}
	
	
	/**
	 * Possible values for subject and objects
	 * @param session_id
	 * @return
	 */
	public TreeSet<Instance> getIndividualsByType(String session_id, String type){
		
		//TODO we may want to filter by type
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
	
	
	
	//TODO return literals? Only if there is a range of possible values: e.g. boolean, list of companies, etc. Check projection
	public TreeSet<String> getAllowedValues(String session_id){
		return null;
		
		//TODO Allow only if given predicate?
		
	}
	
	
	
	public TreeSet<Property> getPredicates(String session_id){
		TreeSet<Property> predicates = new TreeSet<Property>();
		
		predicates.addAll(getDataPredicates(session_id));
		predicates.addAll(getObjectPredicates(session_id));
		
		return predicates;
		
	}
	
	
	public TreeSet<Property> getPredicatesForSubject(String session_id, String subject_iri){
		TreeSet<Property> predicates = new TreeSet<Property>();
		
		predicates.addAll(getDataPredicatesForSubject(session_id, subject_iri));
		predicates.addAll(getObjectPredicatesForSubject(session_id, subject_iri));
		
		return predicates;
		
	}
	
	
	
	
	
	
	
	public TreeSet<DataProperty> getDataPredicates(String session_id){
		return sessionManager.getSession(session_id).getDataPredicates();
	}
	
	
	public TreeSet<ObjectProperty> getObjectPredicates(String session_id){
		return sessionManager.getSession(session_id).getObjectPredicates();
	}
	
	
	public TreeSet<DataProperty> getDataPredicatesForSubject(String session_id, String subject_iri){
		
		//TODO When storing objects and types, store rdf:type but also the direct type for convenience
		
		//TODO Get facet predicates for the type of subject
		return null;
	}
	
	
	public TreeSet<ObjectProperty> getObjectPredicatesForSubject(String session_id, String subject_iri){
		//TODO Get neighbours for the type of subject
		return null;
	}

	
	
	
	
	
	
	/**
	 * Saves annotations from interface. 
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
		saveDataModel(data_model, session_id);
		
		
	}
	
	
	protected void addTriplesForAnnotations(AnnotationGraphModel data_model, Set<Triple> triples) {
		
		//Add triples to model
		for (Triple triple : triples) {
			
			//is_visually_represented triple already added (internally) when associating object to selection shape or to image
			
			if (triple.isTypeDefinitionTriple())
				data_model.addTypeTriple(triple.getSubject().getIri(), triple.asTypeDefinitionTriple().getObject().getIri());
			else if(triple.isDataPropertyTriple())
				data_model.addLiteralTriple(
						triple.getSubject().getIri(), triple.asDataPropertyTriple().getPredicate().getIri(), 
						triple.asDataPropertyTriple().getObject().getValue(), triple.asDataPropertyTriple().getObject().getDatatypeString());	
			else if (triple.isObjectPropertyTriple())
				data_model.addObjectTriple(
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
				GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HAS_PHYISICAL_LOCATION), 
				gimg.getLocation());
		
		
		
		
		//Save new triples
		saveDataModel(data_model, session_id);
		
	}
	
	
	
	
	public String getNewSelectionShapeURI() {
		
		//Create URI for shape
		return getNewResourceURI("shape");
		
	}
	
	
	
	public String getNewURIForObject(String typeName, String labelName) {
		
		//Create URI for object
		//e.g.: typeName=Well, labelName=well 123
		//return getNewResourceURI(typeName.toLowerCase() + "-" + labelName.replaceAll("\\s+","").toLowerCase());
		return getNewResourceURI(typeName.toLowerCase());
	}
	
	
	
	
	
		
	
	
	
	
	public Set<Triple> saveNewShapeAndObject(String session_id, String image_uri, AbstractShape shape, String type_object_uri, String name_object){
		return saveNewShapeAndObject(session_id, image_uri, shape, getNewSelectionShapeURI(), type_object_uri, name_object);
	}
	
	
	
	
	
	public Set<Triple> saveNewShapeAndObject(String session_id, String image_uri, AbstractShape shape, String uri_shape, String type_object_uri, String name_object){
		
		
		//Load model
		AnnotationGraphModel data_model = new AnnotationGraphModel();
		
		String data_file_path = sessionManager.getSession(session_id).getDataFilePath();
		data_model.loadModelFromFile(data_file_path);
		
		//Add new triple to model
		String shape_type = addNewShapeTriples(data_model, image_uri, shape, uri_shape);
		String uri_object = addNewObjectTriples(data_model, uri_shape, type_object_uri, name_object);
	
		
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
								GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED)),
						sessionManager.getSession(session_id).createInstance(uri_shape, shape_type)
						));
		
		
		DataProperty dp = new DataProperty(URIUtils.RDFS_LABEL);
		dp.setLabel("rds:label");
		triplesForInterface.add(
				new DataPropertyTriple(
						instance,
						dp,
						new LiteralValue(name_object)
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
	
	
	
	protected String addNewObjectTriples(AnnotationGraphModel data_model, String shape_uri, String type_object_uri, String name_object) {

		String uri_object = getNewURIForObject(URIUtils.getEntityLabelFromURI(type_object_uri), name_object);
		
		//Instance instance = new Instance(uri_object);
		//instance.setLabel(name_object);
		//instance.setClassType(type_object_uri);
		//Concept type = new Concept(type_object_uri);
		//new TypeDefinitionTriple(instance, type);
		
		
		//Type object
		data_model.addTypeTriple(uri_object, type_object_uri);
		
		//Label_name
		data_model.addLabelTriple(uri_object, name_object);
		
		//Visually represented
		data_model.addObjectTriple(uri_object, 
				GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IS_VISUALLY_REPRESENTED),
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
		String shape_type = GIC_URIUtils.getURIForOntologyEntity(
				StringUtils.capitalize(shape.getShapeType().toString().toLowerCase()));
		
		data_model.addTypeTriple(uri_shape, shape_type);
		
		
		//Link image with shape/selection
		data_model.addObjectTriple(uri_shape, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.IS_SELECTION_OF), image_uri);
		
		//Add points to shape
		String point_uri;
		int i = 0;
		for (WPointF point : shape.getPoints()){
			point_uri = getNewResourceURI("point");
			
			//Type Point
			data_model.addTypeTriple(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.POINT));
			
			//link between shape and points
			data_model.addObjectTriple(uri_shape, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASPOINT), point_uri);
			
			//coordinates
			data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASX), String.valueOf(point.getX()), URIUtils.XSD_DOUBLE);
			data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASY), String.valueOf(point.getY()), URIUtils.XSD_DOUBLE);
			
			//For circles and polygons with 5 or more points the order of points matters
			data_model.addLiteralTriple(point_uri, GIC_URIUtils.getURIForOntologyEntity(GIC_URIUtils.HASPOINTORDER), String.valueOf(i), URIUtils.XSD_INTEGER);
			
			//System.out.println(i + " " + point.getX() + "  " + point.getY());
			i++;
		
			
			
		}
		
		return shape_type;
		
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
