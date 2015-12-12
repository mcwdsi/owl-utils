package uf.bmi.ontology.owl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;

public class GetAnnotationForIri extends GeneralOwlFileProcessor {
	
	IRI annotationPropertyIri;
	
	File output;
	File iriFile;
	
	ArrayList<String> outputLines = new ArrayList<String>();
	
	public GetAnnotationForIri(File configFile) {
		super(configFile);
	}

	@Override
	public void readProcessingSpecificConfiguration() {
		String annotationPropertyIriTxt = p.getProperty("annotation_property_iri");
		
		annotationPropertyIri = IRI.create(annotationPropertyIriTxt);
		
		String outputDir = p.getProperty("output_dir");
		String outputFile = p.getProperty("output_file");
		
		output = new File(outputDir + File.separator + outputFile);
		
		String iriFileDirTxt = p.getProperty("iri_file_dir");
		String iriFileTxt = p.getProperty("iri_file");
		iriFile = new File (iriFileDirTxt + File.separator + iriFileTxt);
	}

	@Override
	public void doMainProcessing() {
		try {
			FileReader fr  = new FileReader(iriFile);
			LineNumberReader lnr = new LineNumberReader(fr);
			
			OWLAnnotationProperty ap = dataFactory.getOWLAnnotationProperty(annotationPropertyIri);
			
			String line;
			while ((line=lnr.readLine())!=null) {
				System.out.print("Processing line " + lnr.getLineNumber() + ": ");
				String[] flds = line.split("\t");
				
				System.out.print(flds[4] + " = ");
				IRI nextIri = IRI.create(flds[4]);
				OWLClass c = dataFactory.getOWLClass(nextIri);
				
				Set<OWLAnnotation> as = c.getAnnotations(ontology, ap);
				if (as != null && !as.isEmpty()) {
					Iterator<OWLAnnotation> i = as.iterator();
					OWLAnnotation a = i.next();
					OWLAnnotationValue av = a.getValue();
					OWLLiteral ol = (OWLLiteral)av;
					
					System.out.print(ol.getLiteral());
					if (flds[2].equals("Y"))
						outputLines.add((ol.getLiteral() + "\t" + flds[0] + "\t" + flds[2] + "\t" + flds[4]));
				}
				
				System.out.println();
			}
			
			lnr.close();
			fr.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		//OWLEntity e = dataFactory.getOWLClass()

	}

	@Override
	public void saveOutput() {
		try {
			FileWriter fw = new FileWriter(output);
			int size = outputLines.size();
			for (int i=0; i<size; i++) {
				fw.write(outputLines.get(i) + "\n");
			}
			fw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void main(String[] args) {
		GetAnnotationForIri a = new GetAnnotationForIri(new File("./src/main/resources/get_annotation_for_iri.txt"));
		a.doWork();
	}

}
