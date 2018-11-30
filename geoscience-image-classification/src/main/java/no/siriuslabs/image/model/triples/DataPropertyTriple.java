package no.siriuslabs.image.model.triples;

import uio.ifi.ontology.toolkit.projection.model.entities.DataProperty;
import uio.ifi.ontology.toolkit.projection.model.entities.Instance;
import uio.ifi.ontology.toolkit.projection.model.entities.LiteralValue;

public class DataPropertyTriple extends Triple<DataProperty, LiteralValue>{

	public DataPropertyTriple(Instance s, DataProperty p, LiteralValue o) {
		super(s, p, o);
	}

	@Override
	public boolean isTypeDefinitionTriple() {
		return false;
	}

	@Override
	public boolean isObjectPropertyTriple() {
		return false;
	}

	@Override
	public boolean isDataPropertyTriple() {
		return true;
	}

	@Override
	public TypeDefinitionTriple asTypeDefinitionTriple() {
		throw new RuntimeException("Not a type definition triple");
	}

	@Override
	public ObjectPropertyTriple asObjectPropertyTriple() {
		throw new RuntimeException("Not a object property triple");
	}

	@Override
	public DataPropertyTriple asDataPropertyTriple() {
		return this;
	}

}
