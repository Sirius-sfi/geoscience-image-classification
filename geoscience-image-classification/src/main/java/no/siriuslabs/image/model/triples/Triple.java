package no.siriuslabs.image.model.triples;

import uio.ifi.ontology.toolkit.projection.model.entities.Instance;

/**
 * Generic Triple type
 * @author ejimenez-ruiz
 *
 * @param <P>
 * @param <O>
 */
public abstract class Triple<P,O> {
	
	
	private Instance subject;
	
	private P predicate;

	private O object;

	public Triple(Instance s, P p, O o) {
		setSubject(s);
		setPredicate(p);
		setObject(o);
	}
	
	
	public abstract boolean isTypeDefinitionTriple();
	
	public abstract boolean isObjectPropertyTriple();
	
	public abstract boolean isDataPropertyTriple();
	
	public abstract TypeDefinitionTriple asTypeDefinitionTriple();
	
	public abstract ObjectPropertyTriple asObjectPropertyTriple();
	
	public abstract DataPropertyTriple asDataPropertyTriple();

	public Instance getSubject() {
		return subject;
	}

	public void setSubject(Instance subject) {
		this.subject = subject;
	}

	public P getPredicate() {
		return predicate;
	}

	public void setPredicate(P predicate) {
		this.predicate = predicate;
	}

	public O getObject() {
		return object;
	}

	public void setObject(O object) {
		this.object = object;
	}
	
	

}
