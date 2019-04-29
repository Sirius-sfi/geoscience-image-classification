package no.siriuslabs.image.model;

import java.util.Calendar;
import java.util.Random;

import org.json.JSONObject;

import uio.ifi.ontology.toolkit.projection.model.entities.Instance;

public abstract class Image extends Instance{

	
	private String contributor;
	private String dateSubmission;
	
	private String relativeImagePath = null;
	private String absoluteImagePath = null;
	
	private String location; //file name
	private String type_label; //label of the type of the image

	/**
	 * Width of the image in pixels.
 	 */
	private int width = 0;
	/**
	 * Height of the image in pixels.
	 */
	private int height = 0;
	
	
	public Image() {
		
		Random randomNum = new Random();
		int random = 1 + randomNum.nextInt(1000);
		
		setIri(getBaseURIResources() + "#" + getBaseNameResources() + "-"+ random + Calendar.getInstance().getTimeInMillis());
	
		
	}
	
	
	public Image(Instance inst) {
		setIri(inst.getIri());
		setLabel(inst.getLabel());
		setSynonyms(inst.getSynonyms());
		setClassType(inst.getClassType());
		setDescription(inst.getDescription());
	}
	
	
	public Image(String uri){
		setIri(uri);
	}

	

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	
	public String getTypeLabel() {
		return type_label;
	}

	public void setTypeLabel(String label) {
		type_label = label;
	}


	


	
	
	public String toString() {
		return getIri() + " - "+ cls_type + " - " + name + " - " + description  + " - " + location;
	}
	
	
	
	public abstract String getBaseURIResources();
	
	public abstract String getBaseNameResources();
	
	
	
	@Override
	public JSONObject toJSON() {
		
		JSONObject obj = super.toJSON();
		
		obj.put("location", getLocation());
		obj.put("type_label", getTypeLabel());

		return obj;
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

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
//public abstract void covertToTriples();


	public String getContributor() {
		return contributor;
	}


	public void setContributor(String contributor) {
		this.contributor = contributor;
	}


	public String getDateSubmission() {
		return dateSubmission;
	}


	public void setDateSubmission(String dateSubmission) {
		this.dateSubmission = dateSubmission;
	}
	
	
}
