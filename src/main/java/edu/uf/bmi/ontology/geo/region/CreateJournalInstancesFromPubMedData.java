package edu.uf.bmi.ontology.geo.region;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;

import org.coode.owlapi.rdf.model.RDFLiteralNode;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFLiteral;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class CreateJournalInstancesFromPubMedData {

	static IRI instanceTypeIri;
	static File input, output, data, load_first;
	static OWLOntologyManager manager;
	static OWLOntology ontology;
	static OWLDataFactory data_factory;
	
	static OWLAnnotationProperty rdfsLabelProp;
	
	static String iri_base;
	static String iri_namespace;
	static int iri_counter, iri_id_length;
	
	static ArrayList<OWLAnnotationProperty> annotationProps;
	
	public static void main(String[] args) {
		/* File headers
				pmid
				nlmUniqueId
				issnLinking
				journalTitle
				isoAbbrev
				medlineTA
				country
		 	*/
		manager = OWLManager.createOWLOntologyManager();
		data_factory = manager.getOWLDataFactory();
		rdfsLabelProp = data_factory.getOWLAnnotationProperty(UsefulGeoIris.rdfsLabelIri);
		readConfig();
		try {
			ontology = manager.loadOntology(IRI.create("http://repos.frontology.org/bfo-1.1/raw/4b2b502d1934b75bf89f53f1bf45bc436467bf8f/bfo-1.1.owl"));
			ontology = manager.loadOntologyFromOntologyDocument(load_first);
			ontology = manager.loadOntologyFromOntologyDocument(input);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createInstancesFromDataInOutput(); 
		try {
			manager.saveOntology(ontology, new FileOutputStream(output));
		} catch (OWLOntologyStorageException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected static void readConfig() {
		Properties p = new Properties();  //loadFromFile("./src/main/resources/config.txt");
		try {
			p.load(new FileReader("./src/main/resources/journal-from-text-file-info-config.txt"));
			
			String inputDir = p.getProperty("input_dir");
			String inputFile = p.getProperty("input_file");
			String outputDir = p.getProperty("output_dir");
			String outputFile = p.getProperty("output_file");
			String dataDir = p.getProperty("data_dir");
			String dataFile =p.getProperty("data_file");
			
			String loadFirstFile = p.getProperty("load_first_file");
			
			input = new File(inputDir + File.separator + inputFile);
			output = new File(outputDir + File.separator + outputFile);
			data = new File(dataDir + File.separator + dataFile);
			load_first = new File(inputDir + File.separator + loadFirstFile);
			
			iri_base = p.getProperty("iri_base");
			iri_namespace = p.getProperty("iri_namespace");
			iri_counter = Integer.parseInt(p.getProperty("starting_iri_number"));
			iri_id_length = Integer.parseInt(p.getProperty("iri_id_length"));
			
			annotationProps = new ArrayList<OWLAnnotationProperty>();
			String annPropInfo = p.getProperty("field_annotation_properties");
			String[] fieldAnnInfo = annPropInfo.split(Pattern.quote(","));
			
			for (String fieldAnnProp : fieldAnnInfo) {
				System.out.println(fieldAnnProp);
				String[] info = fieldAnnProp.split(Pattern.quote(";"));
				String iriText = info[1].trim();
				int pos = Integer.parseInt(info[0]) - 1;
				System.out.println("info[0]="+pos + ",info[1]="+iriText);
				OWLAnnotationProperty oap = data_factory.getOWLAnnotationProperty(IRI.create(iriText));
				System.out.println(oap);
				annotationProps.add(pos, oap);
			}
			
			instanceTypeIri = IRI.create((String)p.get("instanceType"));
			
			System.out.println(iri_base + "," + iri_namespace + "," + iri_counter + "," + iri_id_length);
			System.out.println(nextIri());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static IRI nextIri() {
		String iriText = iri_base + iri_namespace + "_";
		int id_len = Integer.toString(iri_counter).length();
		int diff = iri_id_length - id_len;
		for (int i=0; i<diff; i++) {
			iriText = iriText + "0";
		}
		iriText = iriText + iri_counter;
		iri_counter++;
		return IRI.create(iriText);
	}
	
	static void createInstancesFromDataInOutput() {
		FileReader fr;
		try {
			fr = new FileReader(data);
			LineNumberReader lnr = new LineNumberReader(fr);
			String line;
			while ((line=lnr.readLine())!=null) {
				
				String[] fields = line.split(Pattern.quote("\t"));
				System.out.println(fields.length);
				
				OWLNamedIndividual i = createIndividual();
				
				int counter = 0;
				for (String field : fields) {
					if (!field.equals("")) {
						OWLLiteral ol = data_factory.getOWLLiteral(field);
						OWLAnnotationProperty oap = annotationProps.get(counter);
						OWLAnnotation oa = data_factory.getOWLAnnotation(oap, ol);
						OWLAnnotationAxiom oaa = data_factory.getOWLAnnotationAssertionAxiom(i.getIRI(), oa);
						manager.addAxiom(ontology, oaa);	
					
						if (oap.getIRI().toString().equals("http://purl.org/dc/elements/1.1/title")) {
							OWLLiteral ll = data_factory.getOWLLiteral(field);
							//data_factory.
							OWLAnnotation label = data_factory.getOWLAnnotation(rdfsLabelProp, ll);
							OWLAnnotationAxiom labelAx = data_factory.getOWLAnnotationAssertionAxiom(i.getIRI(), label);
							manager.addAxiom(ontology, labelAx);
						}
					}
					
					counter++;
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static OWLNamedIndividual createIndividual() {
		OWLNamedIndividual ni = data_factory.getOWLNamedIndividual(nextIri());
		createAndAddClassAssertion(ni);		
		return ni;
	}
	
	
	private static void createAndAddClassAssertion(OWLNamedIndividual ni) {
		OWLClass c = data_factory.getOWLClass(instanceTypeIri);
		OWLClassAssertionAxiom ocaa = data_factory.getOWLClassAssertionAxiom(c, ni);
		manager.addAxiom(ontology, ocaa);
	}
	
}
