package no.siriuslabs.image.model;

import uio.ifi.ontology.toolkit.projection.model.entities.Instance;

/**
 * We create a class for Geological Images as they are our first citizens
 * @author ejimenez-ruiz
 *
 */
public class GeologicalImage extends Image{

	

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
		return GIC_URIUtils.BASE_URI_GEOLOGICAL_RESOURCES;
	}


	@Override
	public String getBaseNameResources() {
		return "geological-image";
	}

	
}
