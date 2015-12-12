package edu.uf.bmi.ontology.geo.region;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import uf.bmi.ontology.owl.GeneralOwlFileProcessor;

public class ConnectPublicationInstancesToJournalInstances extends GeneralOwlFileProcessor {

	OWLClass pubClass;
	OWLClass journalClass;
	
	OWLAnnotationProperty dcTitle;
	OWLAnnotationProperty adHocTitle;
	
	OWLObjectProperty propContPartOf;
	
	HashMap<String, OWLNamedIndividual> jTextToJIndividual;
	
	File loadFirst;
	File output;
	
	public ConnectPublicationInstancesToJournalInstances(File configFile) {
		super(configFile);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		File configFile = new File("./src/main/resources/connect-pubs-to-journals-config.txt");
		ConnectPublicationInstancesToJournalInstances c = new ConnectPublicationInstancesToJournalInstances(configFile);
		c.doWork();
	}

	@Override
	public void readProcessingSpecificConfiguration() {
		String pubClassIri = p.getProperty("publication_class_iri");
		String journalClassIri = p.getProperty("journal_class_iri");
		String dcTitleIri = p.getProperty("journal_name_annotation_iri");
		String adHocTitleIri = p.getProperty("pub_journal_name_annotation_iri");
		String propContPartOfIri = p.getProperty("part_of_relation_iri");
		
		String outputDir = p.getProperty("output_dir");
		String outputFile = p.getProperty("output_file");
		
		output = new File(outputDir + File.separator + outputFile);
		
		pubClass = dataFactory.getOWLClass(IRI.create(pubClassIri));
		journalClass = dataFactory.getOWLClass(IRI.create(journalClassIri));
		dcTitle = dataFactory.getOWLAnnotationProperty(IRI.create(dcTitleIri));
		adHocTitle = dataFactory.getOWLAnnotationProperty(IRI.create(adHocTitleIri));
		propContPartOf = dataFactory.getOWLObjectProperty(IRI.create(propContPartOfIri));
	}

	@Override
	public void doMainProcessing() {
		//hash all the journal instances by their NLM abbreviated title, and store their URI
		hashJournalInstances();
		
		//query for all publication instances
		Iterator<OWLIndividual> is = pubClass.getIndividuals(ontology).iterator();
		
		System.out.println("Processing publications...");
		int cPub = 0;
		//iterate through publication instances
		while (is.hasNext()) {
			cPub++;
			System.out.print("Processing publication...");
			OWLIndividual i = is.next();
			if (i instanceof OWLNamedIndividual) {
				//get next publication with IRI
				OWLNamedIndividual ni = i.asOWLNamedIndividual();
				System.out.println(ni.getIRI());
				
				 //get it's journal annotation (lookup URI for that...)
				Iterator<OWLAnnotation> oas = ni.getAnnotations(ontology, adHocTitle).iterator();
				System.out.println("\t" + adHocTitle.getIRI());
		        
				if (oas.hasNext()) {
					OWLAnnotation oa = oas.next();
					//get value of annotation
					OWLAnnotationValue oav = oa.getValue();
					if (oav instanceof OWLLiteral) {
						OWLLiteral ol = (OWLLiteral)oav; 
						String val = ol.getLiteral();
						val = val.replaceAll(Pattern.quote("."), "").toLowerCase();
						System.out.println("\tjournal for pub is " + val);
						//find the journal with that title (in hash)
						OWLNamedIndividual jInd = jTextToJIndividual.get(val);
						
						 //create 'proper continuant part of' relation from publication to article (look up that URI)
						if (jInd != null) {
							OWLObjectPropertyAssertionAxiom oopaa = dataFactory.getOWLObjectPropertyAssertionAxiom(propContPartOf, ni, jInd);
							manager.addAxiom(ontology, oopaa);
						}
						System.out.println("\tConnected pub with title " + val + " to journal individual with that title: " + (jInd != null));
						
					     //delete journal title annotation (perhaps wait until the guys are pulling journal 
					      // title from the journal instance, as otherwise it'll probably break the system.
					}
				} else {
					System.out.println("\tFailed to get journal title for pub");
				}
			}
		}
		System.out.println("done.  \nThere are " + cPub + " publications.");
	}
	
	protected void hashJournalInstances() {
		jTextToJIndividual = new HashMap<String, OWLNamedIndividual>();
		
		Iterator<OWLIndividual> is = journalClass.getIndividuals(ontology).iterator();
		while (is.hasNext()) {
			OWLIndividual i = is.next();
			if (i instanceof OWLNamedIndividual) {
				OWLNamedIndividual ni = i.asOWLNamedIndividual();
				Iterator<OWLAnnotation> oas = ni.getAnnotations(ontology, dcTitle).iterator();
			
				if (oas.hasNext()) {
					OWLAnnotation oa = oas.next();
					OWLAnnotationValue oav = oa.getValue();
					if (oav instanceof OWLLiteral) {
						OWLLiteral ol = (OWLLiteral)oav; 
						String val = ol.getLiteral().toLowerCase();
						jTextToJIndividual.put(val, ni);
						System.out.println("hashed " + val);
					}
				}
			}
		}
	}

	@Override
	public void saveOutput() {
		// TODO Auto-generated method stub
		try {
			manager.saveOntology(ontology, new FileOutputStream(output));
		} catch (OWLOntologyStorageException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override public void doWork() {
		//manager.loadOntology(IRI.create("https://bitbucket.org/uamsdbmi/bfo-1.1/raw/4b2b502d1934b75bf89f53f1bf45bc436467bf8f/bfo-1.1.owl"));
		loadFirst = new File("/Users/hoganwr/devel/ontology/obc-ide-owl-files/obc-ide-indexing-instances-and-classes.owl");
		try {
			manager.loadOntology(IRI.create("http://repos.frontology.org/bfo-1.1/raw/4b2b502d1934b75bf89f53f1bf45bc436467bf8f/bfo-1.1.owl"));
			manager.loadOntologyFromOntologyDocument(loadFirst);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.doWork();
	}

}
