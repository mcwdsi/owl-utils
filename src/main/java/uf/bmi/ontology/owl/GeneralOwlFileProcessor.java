package uf.bmi.ontology.owl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public abstract class GeneralOwlFileProcessor {
	
	protected OWLOntology ontology;
	protected OWLOntologyManager manager;
	protected OWLDataFactory dataFactory;
	
	protected File configFile;
	
	protected String inputType;
	protected File inputFile;
	protected IRI inputIRI;
	
	protected Properties p;
	
	public GeneralOwlFileProcessor(File configFile) {
		this.configFile = configFile;
		manager = OWLManager.createOWLOntologyManager();
		dataFactory = manager.getOWLDataFactory();
	}
	
	public void doWork() {
		readConfiguration();
		loadOwlFile();
		doMainProcessing();
		saveOutput();
	}
	
	public void readConfiguration() {
		p = new Properties();  //loadFromFile("./src/main/resources/config.txt");
		try {
			p.load(new FileReader(configFile));
			
			inputType = p.getProperty("input_type").toLowerCase();
			if (!inputType.equals("file") && !inputType.equals("iri")) {
				throw new IllegalArgumentException("input type must be 'file' or 'iri'");
			}
			
			if (inputType.equals("file")) {
				String inputDir = p.getProperty("input_dir");
				String inputFileName = p.getProperty("input_file");
				inputFile = new File(inputDir + File.separator + inputFileName);
			} else if (inputType.equals("iri")) {
				inputIRI = IRI.create(p.getProperty("input_iri"));
			}
			
		} catch (FileNotFoundException fne) {
			fne.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			 
		readProcessingSpecificConfiguration();
	}
	
	public void loadOwlFile() {
		if (inputType.equals("file")) {
			try {
				ontology = manager.loadOntologyFromOntologyDocument(inputFile);
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (inputType.equals("iri")) {
			try {
				ontology = manager.loadOntology(inputIRI);
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public abstract void readProcessingSpecificConfiguration();
	public abstract void doMainProcessing();
	public abstract void saveOutput();

}
