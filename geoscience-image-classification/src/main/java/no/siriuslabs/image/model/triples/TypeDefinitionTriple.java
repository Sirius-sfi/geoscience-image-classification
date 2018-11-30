package no.siriuslabs.image.model.triples;

import uio.ifi.ontology.toolkit.projection.model.entities.Concept;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.ObjectProperty;
import uio.ifi.ontology.toolkit.projection.utils.URIUtils;

public class TypeDefinitionTriple extends Triple<ObjectProperty, Concept>{

	public TypeDefinitionTriple(Instance s, Concept o) {
		this(s, new ObjectProperty(URIUtils.RDF_TYPE), o);
	}
	
	public TypeDefinitionTriple(Instance s, ObjectProperty p, Concept o) {
		super(s, p, o);
	}

	@Override
	public boolean isTypeDefinitionTriple() {
		return true;
	}

	@Override
	public boolean isObjectPropertyTriple() {
		return false;
	}

	@Override
	public boolean isDataPropertyTriple() {
		return false;
	}

	@Override
	public TypeDefinitionTriple asTypeDefinitionTriple() {
		return this;
	}

	@Override
	public ObjectPropertyTriple asObjectPropertyTriple() {
		throw new RuntimeException("Not a object property triple");
	}

	@Override
	public DataPropertyTriple asDataPropertyTriple() {
		throw new RuntimeException("Not a data property triple");
	}

}
