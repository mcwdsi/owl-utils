package uf.bmi.ontology.owl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class CopyDefinitionToElucidation extends GeneralOwlFileProcessor {

	IRI defPropertyIri;
	IRI elucPropertyIri;
	
	File output;
	
	public CopyDefinitionToElucidation(File configFile) {
		super(configFile);
	}

	@Override
	public void readProcessingSpecificConfiguration() {
		String defPropertyIriTxt = p.getProperty("definition_iri");
		String elucPropertyIriTxt = p.getProperty("elucidation_iri");
		
		defPropertyIri = IRI.create(defPropertyIriTxt);
		elucPropertyIri = IRI.create(elucPropertyIriTxt);
		
		String outputDir = p.getProperty("output_dir");
		String outputFile = p.getProperty("output_file");
		
		output = new File(outputDir + File.separator + outputFile);
	}

	@Override
	public void doMainProcessing() {
		Iterator<OWLClass> i = ontology.getClassesInSignature().iterator();
		OWLAnnotationProperty elucProp = dataFactory.getOWLAnnotationProperty(elucPropertyIri);
		int countFix=0;
		while (i.hasNext()) {
			OWLClass c = i.next();
			if (!c.getIRI().toString().contains("APOLLO_SV")) continue;
			Iterator<OWLAnnotation> j = c.getAnnotations(ontology).iterator();
			boolean hasDef = false;
			boolean hasEluc = false;
			OWLAnnotationValue defValue = null;
			
			while (j.hasNext()) {
				OWLAnnotation a = j.next();
				if (!hasDef) {
					hasDef = a.getProperty().getIRI().equals(defPropertyIri);
					defValue = a.getValue();
				}
				hasEluc = hasEluc || a.getProperty().getIRI().equals(elucPropertyIri);
			}
			
			if (hasDef && !hasEluc) {
				OWLAnnotation eluc = dataFactory.getOWLAnnotation(elucProp, defValue);
				OWLAnnotationAssertionAxiom ax = dataFactory.getOWLAnnotationAssertionAxiom(c.getIRI(), eluc);
				manager.addAxiom(ontology, ax);
				System.out.println(c.getIRI().toString());
				countFix++;
			}
		}
		System.out.println(countFix);
	}

	@Override
	public void saveOutput() {
		try {
			manager.saveOntology(ontology, new FileOutputStream(output));
		} catch (OWLOntologyStorageException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		CopyDefinitionToElucidation c = new CopyDefinitionToElucidation(new File("./src/main/resources/copy_def_to_eluc.txt"));
		c.doWork();
	}

}
