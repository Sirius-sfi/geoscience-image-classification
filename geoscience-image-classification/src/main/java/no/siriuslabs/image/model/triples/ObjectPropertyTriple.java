package no.siriuslabs.image.model.triples;

import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.ObjectProperty;

public class ObjectPropertyTriple extends Triple<ObjectProperty, Instance>{

	public ObjectPropertyTriple(Instance s, ObjectProperty p, Instance o) {
		super(s, p, o);
		
	}

	@Override
	public boolean isTypeDefinitionTriple() {
		return false;
	}

	@Override
	public boolean isObjectPropertyTriple() {
		return true;
	}

	@Override
	public boolean isDataPropertyTriple() {
		return false;
	}

	@Override
	public TypeDefinitionTriple asTypeDefinitionTriple() {
		throw new RuntimeException("Not a type definition triple");
	}

	@Override
	public ObjectPropertyTriple asObjectPropertyTriple() {
		return this;
	}

	@Override
	public DataPropertyTriple asDataPropertyTriple() {
		throw new RuntimeException("Not a data property triple");
	}

}
