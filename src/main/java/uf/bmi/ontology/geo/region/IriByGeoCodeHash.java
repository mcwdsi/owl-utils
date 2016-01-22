package edu.uf.bmi.ontology.geo.region;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;



public class IriByGeoCodeHash {
	File f;
	OWLOntologyManager manager;
	OWLOntology ontology;
	OWLDataFactory dataFactory;
	IRI annPropIri;
	
	HashMap<String, IRI> codeToIri;
	
	
	public IriByGeoCodeHash(File f, IRI annPropIriForCode) {
		this.f = f;
		annPropIri = annPropIriForCode;
		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();
		codeToIri = new HashMap<String,IRI>();
	}
	
	public void buildCache()  {
		try {
			ontology = manager.loadOntologyFromOntologyDocument(f);
			System.out.println(manager.getOntologies().size() + " ontologies");
			Iterator<OWLOntology> k = manager.getOntologies().iterator();
			
			while (k.hasNext()) {
				ontology = k.next();
			
				Iterator<OWLNamedIndividual> i = ontology.getIndividualsInSignature().iterator();
			
				//OWLAnnotationProperty codeProp = dataFactory.getOWLAnnotationProperty(annPropIri);
			
				while (i.hasNext()) {
					OWLNamedIndividual ni = i.next();
					Iterator<OWLAnnotationAssertionAxiom> j = ontology.getAnnotationAssertionAxioms(ni.getIRI()).iterator(); //ni.getAnnotations(ontology).iterator();
					//System.out.println("Individual IRI: " + ni.getIRI());
				
					while (j.hasNext()) {
						OWLAnnotation a = j.next().getAnnotation();
						//System.out.println("\tAnnotation property IRI = " + a.getProperty().getIRI());
						if (a.getProperty().getIRI().equals(annPropIri)) {
							OWLAnnotationValue av = a.getValue();
							if (av instanceof OWLLiteral) {
								OWLLiteral ol = (OWLLiteral)av;
								if (!codeToIri.containsKey(ol.getLiteral()))
									codeToIri.put(ol.getLiteral(),ni.getIRI());
								//System.out.println(ol.getLiteral());
								else 
									System.err.println(ol.getLiteral() + " already has IRI"
										+ ni.getIRI());
								
							} else
								System.err.println("annotation value is not a literal");
						}
				
					} //j
				} //i
			} //k
			
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public IRI getIriforCode(String code) {
		return codeToIri.get(code);
	}
	
	public int size() {
		return codeToIri.size();
	}
}
