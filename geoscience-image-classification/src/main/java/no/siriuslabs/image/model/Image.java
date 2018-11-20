package no.siriuslabs.image.model;

import java.util.Calendar;
import java.util.Random;

import org.json.JSONObject;

import uio.ifi.ontology.toolkit.projection.model.entities.Instance;

public abstract class Image extends Instance{

	
	private String location; //file name
	private String type_label; //label of the type of the image
	
	
	public Image() {
		
		Random randomNum = new Random();
		int random = 1 + randomNum.nextInt(1000);
		
		setIri(getBaseURIResources() + "#" + getBaseNameResources() + "-"+ random + Calendar.getInstance().getTimeInMillis());
	
		
	}
	
	
	public Image(Instance inst) {
		setIri(inst.getIri());
		setLabel(inst.getLabel());
		setSynonyms(inst.getSynonyms());
		setType(inst.getType());
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
		return getIri() + " - "+ type + " - " + name + " - " + description  + " - " + location;
	}
	
	
	
	public abstract String getBaseURIResources();
	
	public abstract String getBaseNameResources();
	
	
	
	@Override
	public JSONObject toJSON() {
		
		JSONObject obj = super.toJSON();
		
		obj.put("location", getLocation());

		return obj;
	}
	
	
	//public abstract void covertToTriples();
	
	
}
