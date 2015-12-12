package edu.uf.bmi.ontology.geo.region;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/*
 * 
 * 
 * need to:
 * 
 * 1. open the instances owl file
 * 2. get all the instances
 * 3. for each instance
 * 4. is it an instance of geopolitical organization (or whatever)
 * 5. if yes, get its annotations
 * 6. create a new instance called "region of x" where rdfs:label is "x"
 * 7. copy annotations over - ISO, INCITS, etc.
 * 8. create any proper continuant part of relationships necessary
 * 9. write new instances to file
 */
public class CreateRegionInstancesFromFileOfOrgInstances {
	
	static File input, output;
	static OWLOntologyManager manager;
	static OWLOntology inputOntology, outputOntology;
	static IRI outputOntologyIri;
	static OWLDataFactory data_factory;
	
	static String iri_base;
	static String iri_namespace;
	static int iri_counter, iri_id_length;
	
	public static void main(String[] args) {
		readConfig();
		manager = OWLManager.createOWLOntologyManager();
		data_factory = manager.getOWLDataFactory();
		try {
			inputOntology = manager.loadOntologyFromOntologyDocument(input);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createOutputOntology();
		processInputIntoOutput(); 
		saveOutputOntology();
		
		System.out.println("Output ontology has " + outputOntology.getAxiomCount() + " axioms.");
		System.out.println(nextIri() + "," + nextIri() + "," + nextIri());
	}

	private static void processInputIntoOutput() {
		
		Iterator<OWLNamedIndividual> is = inputOntology.getIndividualsInSignature().iterator();
		while (is.hasNext()) {
			OWLNamedIndividual i = is.next();
			//make sure the individual is asserted to be an instance of a particular class
			if (checkType(i)) {
				OWLNamedIndividual ni = createNewIndividualFromExisting(i);
				Iterator<OWLAnnotation> as = i.getAnnotations(inputOntology).iterator();
				while (as.hasNext()) {
					OWLAnnotation a = as.next();
					OWLAnnotationProperty ap = a.getProperty();
					OWLAnnotationValue av = a.getValue();
					OWLLiteral ol = (OWLLiteral)av;
					System.out.println(ap.getIRI() + "   =   " + av + ", " + ol.getLang() + ", " + ol.getDatatype()
							+  ", " + ol.getLiteral());
				}
			}
		}

	}

	private static boolean checkType(OWLNamedIndividual i) {
		Iterator<OWLClassExpression> ts =  i.getTypes(inputOntology).iterator();
		boolean ok = false;
		while (ts.hasNext()) {
			OWLClassExpression t = ts.next();
			ClassExpressionType cet = t.getClassExpressionType();
			if (cet.equals(ClassExpressionType.OWL_CLASS)) {
				OWLClass c = t.getClassesInSignature().iterator().next();
				ok = (c.getIRI().equals(UsefulGeoIris.geoDependencyIri)); 
			}
		}
		//System.out.println(ok);
		return ok;
	}
	
	private static OWLNamedIndividual createNewIndividualFromExisting(OWLNamedIndividual oi) {
		OWLNamedIndividual ni = data_factory.getOWLNamedIndividual(nextIri());
		createAndAddClassAssertion(ni);
		Iterator<OWLAnnotationProperty> oaps = inputOntology.getAnnotationPropertiesInSignature().iterator();
		System.out.println(oaps.hasNext());
		while (oaps.hasNext()) {
			OWLAnnotationProperty oap = oaps.next();
			if (oap.getIRI().equals(UsefulGeoIris.rdfsLabelIri)) {
				addAnnotationsToNewFromOld(oi, ni, oap);
			} else if (oap.getIRI().equals(UsefulGeoIris.iso31661alpha_2_codeIri)) {
				addAnnotationsToNewFromOld(oi, ni, oap);
			} else if (oap.getIRI().equals(UsefulGeoIris.iso31661alpha_3_codeIri)) {
				addAnnotationsToNewFromOld(oi, ni, oap);
			} else if (oap.getIRI().equals(UsefulGeoIris.uNNumericalCodeIri)) {
				addAnnotationsToNewFromOld(oi, ni, oap);
			}
		}
		
		
		return ni;
	}
	
	private static void createAndAddClassAssertion(OWLNamedIndividual ni) {
		OWLClass c = data_factory.getOWLClass(UsefulGeoIris.geoRegionIri);
		OWLClassAssertionAxiom ocaa = data_factory.getOWLClassAssertionAxiom(c, ni);
		manager.addAxiom(outputOntology, ocaa);
	}
	
	private static void addAnnotationsToNewFromOld(OWLNamedIndividual oi, OWLNamedIndividual ni, OWLAnnotationProperty oap) {
		Iterator<OWLAnnotation> oas = oi.getAnnotations(inputOntology, oap).iterator();
		System.out.println(oas.hasNext());
		while (oas.hasNext()) {
			OWLAnnotation oa = oas.next();
			OWLAnnotationValue oav = oa.getValue();
			if (oav instanceof OWLLiteral) {
				OWLLiteral ol = (OWLLiteral)oav; 
				System.out.print(ol.getDatatype());
				System.out.print("\t" + ol.getLang());
				
				String lang = ol.getLang();
				String val = ol.getLiteral();
				if (oap.getIRI().equals(UsefulGeoIris.rdfsLabelIri)) {
					val = "region of " + val;
				}
				OWLDatatype datatype = ol.getDatatype();
				
				
				OWLLiteral nol;
				if (lang != null && !lang.equals("")) {
					nol = data_factory.getOWLLiteral(val, lang);
				} else {
					nol = data_factory.getOWLLiteral(val, datatype);
				}

				System.out.print("\t" + nol.getDatatype());
				System.out.println("\t" + nol.getLang());
				
				OWLAnnotation noa = data_factory.getOWLAnnotation(oap, nol);
				OWLAnnotationAxiom oaa = data_factory.getOWLAnnotationAssertionAxiom(ni.getIRI(), noa);
				manager.addAxiom(outputOntology, oaa);				
				
				
			} else {
				System.out.println("Annotation Value Object Is Of Type: " + oav.getClass());
			}
		}
	}
	
	static void readConfig() {
		Properties p = new Properties();  //loadFromFile("./src/main/resources/config.txt");
		try {
			p.load(new FileReader("./src/main/resources/region-from-org-config.txt"));
			
			String inputDir = p.getProperty("input_dir");
			String inputFile = p.getProperty("input_file");
			String outputDir = p.getProperty("output_dir");
			String outputFile = p.getProperty("output_file");
			
			input = new File(inputDir + File.separator + inputFile);
			output = new File(outputDir + File.separator + outputFile);
			
			outputOntologyIri = IRI.create(p.getProperty("output_ontology_uri"));
			
			iri_base = p.getProperty("iri_base");
			iri_namespace = p.getProperty("iri_namespace");
			iri_counter = Integer.parseInt(p.getProperty("starting_iri_number"));
			iri_id_length = Integer.parseInt(p.getProperty("iri_id_length"));
			
			System.out.println(iri_base + "," + iri_namespace + "," + iri_counter + "," + iri_id_length);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void createOutputOntology() {
		try {
			outputOntology = manager.createOntology(outputOntologyIri);
		} catch (OWLOntologyCreationException e) {
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
	
	static void saveOutputOntology() {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(output);
			manager.saveOntology(outputOntology, fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
