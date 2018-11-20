package no.siriuslabs.image.model;

import uio.ifi.ontology.toolkit.projection.model.entities.Instance;

/**
 * We create a class for Geological Images as they are our first citizens
 * @author ejimenez-ruiz
 *
 */
public class GeologicalImage extends Image{

	private String relativeImagePath = null;
	private String absoluteImagePath = null;

	/**
	 * This constructor creates a new URI for the image
	 */
	public GeologicalImage(){
		super();
			
	}
	
	
	public GeologicalImage(Instance instance){
		super(instance);
			
	}
	
	
	public GeologicalImage(String uri){
		super(uri);
	}


	@Override
	public String getBaseURIResources() {
		return URIUtils.BASE_URI_GEOLOGICAL_RESOURCES;
	}


	@Override
	public String getBaseNameResources() {
		return "geological-image";
	}

	public String getRelativeImagePath() {
		return relativeImagePath;
	}

	public void setRelativeImagePath(String relativeImagePath) {
		this.relativeImagePath = relativeImagePath;
	}

	public String getAbsoluteImagePath() {
		return absoluteImagePath;
	}

	public void setAbsoluteImagePath(String absoluteImagePath) {
		this.absoluteImagePath = absoluteImagePath;
	}
}
