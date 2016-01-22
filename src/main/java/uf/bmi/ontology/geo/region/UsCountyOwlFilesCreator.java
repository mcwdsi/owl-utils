package edu.uf.bmi.ontology.geo.region;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.Properties;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class UsCountyOwlFilesCreator {
	public static void main(String[] args) {
		String configFile = "./src/main/resources/county_iri_props.txt";
		File f = new File (configFile);
		try {
			UsCountyOwlFilesCreator c = new UsCountyOwlFilesCreator(f);
			c.createFiles();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	File f;
	Properties p;
	LineNumberReader countyDataFile;
	String lastUsps = "";
	OWLOntology currentOntology = null;
	
	OWLOntologyManager manager;
	OWLDataFactory factory;
	
	long iriCounter;
	String outputTxt;
	
	//Annotation properties
	IRI uspsIri;
	IRI incits31Iri;
	IRI edPrefTermIri;
	IRI rdfsLabelIri;
	IRI gnisIdIri;
	IRI altTermIri;
	
	//OWLAnnotationProperty uspsProp;
	OWLAnnotationProperty incits31Prop;
	OWLAnnotationProperty edPrefTermProp;
	OWLAnnotationProperty rdfsLabelProp;
	OWLAnnotationProperty gnisIdProp;
	OWLAnnotationProperty altTermProp;
	
	//Object properties
	IRI partOfIri;
	OWLObjectProperty partOf;
	
	IRI bearerOfIri;
	OWLObjectProperty bearerOf;
	
	IRI isAboutIri;
	OWLObjectProperty isAbout;
	
	IRI hasValueSpecIri;
	OWLObjectProperty hasValueSpec;
	
	IRI hasUnitLabelIri;
	OWLObjectProperty hasUnitLabel;
	
	IRI hasMeasValueIri;
	OWLDataProperty hasMeasValue;
	
	IRI occupiesRegionIri;
	OWLObjectProperty occupiesRegion;
	
	IRI hasPropPartIri;
	OWLObjectProperty hasPropPart;
	
	//Classes
	IRI geoRegionIri;
	OWLClass geoRegion;
	
	IRI areaIri;
	OWLClass area;
	
	IRI smdIri;
	OWLClass smd;
	
	IRI valueSpecIri;
	OWLClass valueSpec;
	
	IRI threeDIri;
	OWLClass threeD;
	
	IRI zeroDIri;
	OWLClass zeroD;
	
	IRI latMIri;
	OWLClass latMeasurement;
	
	IRI lonMIri;
	OWLClass lonMeasurement;
	
	//Named individuals
	IRI squareMeterIri;
	OWLNamedIndividual squareMeter;
	
	IRI squareMileIri;
	OWLNamedIndividual squareMile;
	
	IRI angDegreeIri;
	OWLNamedIndividual angularDegree;
	
	//iri Hashes
	IriByGeoCodeHash uspsToIri;
	IriByGeoCodeHash incits31ToIri;
	
	//value specification hashes
	//HashMap<String, 
	
	public UsCountyOwlFilesCreator(File f) throws FileNotFoundException, IOException {
		p = new Properties();
		p.load(new FileReader(f));
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();	
	}
	
	public void createFiles() throws IOException, OWLOntologyCreationException {
		prepInputDataFile();
		prepClassesAndProperties();
		prepIriHashes();
		prepIriCounter();
		processInputDataFile();
	}

	protected void prepInputDataFile() throws FileNotFoundException {
		FileReader fr = new FileReader(p.getProperty("county_data_input_file"));
		countyDataFile = new LineNumberReader(fr);
	}
	
	protected void prepClassesAndProperties() {
		uspsIri = IRI.create(p.getProperty("usps_property_iri"));
		
		incits31Iri = IRI.create(p.getProperty("incits_31_iri"));
		incits31Prop = factory.getOWLAnnotationProperty(incits31Iri);
		
		edPrefTermIri = IRI.create(p.getProperty("editor_preferred_iri"));
		edPrefTermProp = factory.getOWLAnnotationProperty(edPrefTermIri);
		
		rdfsLabelIri = IRI.create(p.getProperty("label_iri"));
		rdfsLabelProp = factory.getOWLAnnotationProperty(rdfsLabelIri);
		
		gnisIdIri = IRI.create(p.getProperty("gnis_id_iri"));
		gnisIdProp = factory.getOWLAnnotationProperty(gnisIdIri);
		
		altTermIri = IRI.create(p.getProperty("alt_term_iri"));
		altTermProp = factory.getOWLAnnotationProperty(altTermIri);
		
		partOfIri = IRI.create(p.getProperty("proper_continuant_part_of_iri"));
		partOf = factory.getOWLObjectProperty(partOfIri);
		
		bearerOfIri = IRI.create(p.getProperty("bearer_of_iri"));
		bearerOf = factory.getOWLObjectProperty(bearerOfIri);
		
		isAboutIri = IRI.create(p.getProperty("is_about_iri"));
		isAbout = factory.getOWLObjectProperty(isAboutIri);
		
		geoRegionIri = IRI.create(p.getProperty("geographical_region_class_iri"));
		geoRegion = factory.getOWLClass(geoRegionIri);
		
		areaIri = IRI.create(p.getProperty("area_class_iri"));
		area = factory.getOWLClass(areaIri);
		
		smdIri = IRI.create(p.getProperty("smd_iri"));
		smd = factory.getOWLClass(smdIri);
		
		valueSpecIri = IRI.create(p.getProperty("value_spec_iri"));
		valueSpec = factory.getOWLClass(valueSpecIri);
		
		hasValueSpecIri = IRI.create(p.getProperty("has_value_spec_iri"));
		hasValueSpec = factory.getOWLObjectProperty(hasValueSpecIri);
		
		hasUnitLabelIri = IRI.create(p.getProperty("has_unit_label_iri"));
		hasUnitLabel = factory.getOWLObjectProperty(hasUnitLabelIri);
		
		hasMeasValueIri = IRI.create(p.getProperty("has_meas_value_iri"));
		hasMeasValue = factory.getOWLDataProperty(hasMeasValueIri);
		
		squareMeterIri = IRI.create(p.getProperty("square_meter_iri"));
		squareMeter = factory.getOWLNamedIndividual(squareMeterIri);
		
		squareMileIri = IRI.create(p.getProperty("square_mile_iri"));
		squareMile = factory.getOWLNamedIndividual(squareMileIri);
		
		angDegreeIri = IRI.create(p.getProperty("angular_degree_iri"));
		angularDegree = factory.getOWLNamedIndividual(angDegreeIri);
		
		latMIri = IRI.create(p.getProperty("lat_measurement_iri"));
		latMeasurement = factory.getOWLClass(latMIri);
		
		lonMIri = IRI.create(p.getProperty("lon_measurement_iri"));
		lonMeasurement = factory.getOWLClass(lonMIri);
		
		threeDIri = IRI.create(p.getProperty("three_dim_region_iri"));
		threeD = factory.getOWLClass(threeDIri);
		
		zeroDIri = IRI.create(p.getProperty("zero_dim_region_iri"));
		zeroD = factory.getOWLClass(zeroDIri);
		
		hasPropPartIri = IRI.create(p.getProperty("has_prop_cont_part_iri"));
		hasPropPart = factory.getOWLObjectProperty(hasPropPartIri);
		
		occupiesRegionIri  = IRI.create(p.getProperty("occupies_spatial_region_iri"));
		occupiesRegion = factory.getOWLObjectProperty(occupiesRegionIri);

	}
	
	protected void prepIriHashes() {
		File uspsFile = new File(p.getProperty("usps_cache_source_file"));
		File incits31File = new File(p.getProperty("incits_31_cache_file"));
		
		uspsToIri = new IriByGeoCodeHash(uspsFile, uspsIri);
		incits31ToIri = new IriByGeoCodeHash(incits31File, incits31Iri);
		
		uspsToIri.buildCache();
		incits31ToIri.buildCache();
		
		//System.out.println("IRI hashes prepped.");
	}
	
	protected void prepIriCounter() {
		iriCounter = Long.parseLong(p.getProperty("starting_iri_number"));
	}
	
	protected void processInputDataFile() throws IOException, OWLOntologyCreationException {
		String line = countyDataFile.readLine(); //throw away header row
		while ((line=countyDataFile.readLine())!=null) {
			String[] flds = line.split("\t");
			if (flds.length == 10) {
				processInputDataLine(flds);
			} else {
				System.err.println("Line " + countyDataFile.getLineNumber() + " has unexpected number of fields: " + flds.length);
			}
		}
	}

	protected void processInputDataLine(String[] flds) throws OWLOntologyCreationException {
		System.out.println(lastUsps + "\t" + flds[0]);
		String usps = flds[0];
		if (!usps.equals(lastUsps)) {
			writeCurrentOntologyToFile();
			createNewOntology(usps);
			outputTxt = "/Users/hoganwr/Dropbox/research/ontology/geopolitical_inventory/county/" +
					"us-" + usps.toLowerCase() + "-county-geography.owl";
		}
		lastUsps = usps;
		if (usps.equals("PR") || usps.equals("DC")) return;
		
		String incits31 = flds[1].trim();
		String gnisId = flds[2].trim();
		String name = flds[3].trim();
		String aLandMeter = flds[4].trim();
		String aWaterMeter = flds[5].trim();
		String aLandMile = flds[6].trim();
		String aWaterMile = flds[7].trim();
		String lat = flds[8].trim();
		String lon = flds[9].trim();
		
		IRI iri = incits31ToIri.getIriforCode(incits31);
		if (iri==null) iri = nextIri();
		
		OWLNamedIndividual county = createAndAnnotateIndividual(iri,usps,incits31,gnisId,name);
		connectCountyToState(county, usps);
		createAreaAndMeasurements(county,name,usps,aLandMeter,aWaterMeter,aLandMile,aWaterMile);
		createLatLon(county,name,usps,lat,lon);
		
		String label = name + ", " + usps;
		System.out.println(label);
		
		
	}
	
	protected void writeCurrentOntologyToFile() {
		if (currentOntology == null) return;
		
		try {
			manager.saveOntology(currentOntology, new FileOutputStream(outputTxt));
			
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void createNewOntology(String usps) throws OWLOntologyCreationException {
		String ontologyIriTxt = "http://purl.obolibrary.org/obo/geo/us-" + usps.toLowerCase() + "-county-geography.owl";
		currentOntology = manager.createOntology(IRI.create(ontologyIriTxt));
		OWLImportsDeclaration d = factory.getOWLImportsDeclaration(IRI.create("http://purl.obolibrary.org/obo/geo/dev/us-integral-geography.owl"));
		manager.applyChange(new AddImport(currentOntology, d));
	}
	
	protected OWLNamedIndividual createAndAnnotateIndividual(IRI iri, String usps, String incits31, String gnisId, String name) {
		OWLNamedIndividual i = null;
		i = factory.getOWLNamedIndividual(iri);
		OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(geoRegion, i);
		manager.addAxiom(currentOntology, ax);
		
		String label = "region of "  + name + ", " + usps;
		
		createAnnotationAndAddToOntology(rdfsLabelProp, label, i);
		createAnnotationAndAddToOntology(edPrefTermProp, label, i);
		createAnnotationAndAddToOntology(incits31Prop, incits31, i);
		createAnnotationAndAddToOntology(gnisIdProp, gnisId, i);
		createAnnotationAndAddToOntology(altTermProp, name, i);
		
		return i;
		
	}

	private void createAnnotationAndAddToOntology(OWLAnnotationProperty prop, String value, OWLIndividual i) {
		OWLLiteral valueLiteral = factory.getOWLLiteral(value);
		OWLAnnotation a = factory.getOWLAnnotation(prop, valueLiteral);
		OWLAnnotationAssertionAxiom ax2 = null;
		if (i instanceof OWLNamedIndividual) {
			ax2 = factory.getOWLAnnotationAssertionAxiom(((OWLNamedIndividual)i).getIRI(), a);
		} else if (i instanceof OWLAnonymousIndividual) {
			ax2 = factory.getOWLAnnotationAssertionAxiom((OWLAnonymousIndividual)i, a);
		}
		manager.addAxiom(currentOntology, ax2);
	}
	
	protected void connectCountyToState(OWLNamedIndividual county, String usps) {
		IRI stateIri = uspsToIri.getIriforCode(usps);
		OWLNamedIndividual state = factory.getOWLNamedIndividual(stateIri);
		OWLObjectPropertyAssertionAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(partOf, county, state);
		manager.addAxiom(currentOntology, ax);
	}
	
	protected void createAreaAndMeasurements(OWLNamedIndividual county, String name, String usps, 
			String aLandMeter, String aWaterMeter, String aLandMile, String aWaterMile) {
		//create land area quality and associate to geo region
		IRI landAreaIri = nextIri();
		OWLNamedIndividual landArea = factory.getOWLNamedIndividual(landAreaIri);
		OWLClassAssertionAxiom ax = factory.getOWLClassAssertionAxiom(area, landArea);
		manager.addAxiom(currentOntology, ax);
		
		String labelLandArea = "land area of region of "  + name + ", " + usps;
		
		createAnnotationAndAddToOntology(rdfsLabelProp, labelLandArea, landArea);
		OWLObjectPropertyAssertionAxiom ax2 = factory.getOWLObjectPropertyAssertionAxiom(bearerOf, county, landArea);
		manager.addAxiom(currentOntology, ax2);
		
		//create water area quality and associate to geo region
		IRI waterAreaIri = nextIri();
		OWLNamedIndividual waterArea = factory.getOWLNamedIndividual(waterAreaIri);
		OWLClassAssertionAxiom ax3 = factory.getOWLClassAssertionAxiom(area, waterArea);
		manager.addAxiom(currentOntology, ax3);
		
		String labelWaterArea = "water area of region of "  + name + ", " + usps;
		
		createAnnotationAndAddToOntology(rdfsLabelProp, labelWaterArea, waterArea);
		OWLObjectPropertyAssertionAxiom ax4 = factory.getOWLObjectPropertyAssertionAxiom(bearerOf, county, waterArea);
		manager.addAxiom(currentOntology, ax4);
		
		String measurementLabel = "measurement of " + labelLandArea + " in square meters";
		createAreaMeasurement(landArea, measurementLabel, aLandMeter, squareMeter);
		
		measurementLabel = "measurement of " + labelWaterArea + " in square meters";
		createAreaMeasurement(waterArea, measurementLabel, aWaterMeter, squareMeter);
		
		measurementLabel = "measurement of " + labelLandArea + " in square miles";
		createAreaMeasurement(landArea, measurementLabel, aLandMile, squareMile);
		
		measurementLabel = "measurement of " + labelWaterArea + " in square miles";
		createAreaMeasurement(waterArea, measurementLabel, aWaterMile, squareMile);
		
	}
	
	private void createAreaMeasurement(OWLNamedIndividual areaQuality,
			String measurementLabel, String areaTxt, OWLNamedIndividual unitLabel) {
		// TODO Auto-generated method stub
		IRI measurementIri = nextIri();
		OWLNamedIndividual measurement = factory.getOWLNamedIndividual(measurementIri);
		
		createAnnotationAndAddToOntology(rdfsLabelProp, measurementLabel, measurement);
		OWLObjectPropertyAssertionAxiom ax1 = factory.getOWLObjectPropertyAssertionAxiom(isAbout, measurement, areaQuality);
		manager.addAxiom(currentOntology, ax1);
		
		OWLClassAssertionAxiom ax2 = factory.getOWLClassAssertionAxiom(smd, measurement);
		manager.addAxiom(currentOntology, ax2);
		
		//value specification
		OWLAnonymousIndividual valueSpecIndividual = factory.getOWLAnonymousIndividual();
		OWLClassAssertionAxiom ax3 = factory.getOWLClassAssertionAxiom(valueSpec, valueSpecIndividual);
		manager.addAxiom(currentOntology, ax3);
		
		
		//has value spec from measurement to value spec
		OWLObjectPropertyAssertionAxiom ax4 = factory.getOWLObjectPropertyAssertionAxiom(hasValueSpec, measurement, valueSpecIndividual);
		manager.addAxiom(currentOntology, ax4);
		//
		
		//has measurement value from measurement to float
		OWLDataPropertyAssertionAxiom ax5 = factory.getOWLDataPropertyAssertionAxiom(hasMeasValue, valueSpecIndividual, Double.parseDouble(areaTxt));
		manager.addAxiom(currentOntology, ax5);
	
		if (unitLabel == squareMile) {
			String vsLabel = areaTxt + " square miles";
			createAnnotationAndAddToOntology(rdfsLabelProp, vsLabel, valueSpecIndividual);
		} else if (unitLabel == squareMeter) {
			String vsLabel = areaTxt + " square meters";
			createAnnotationAndAddToOntology(rdfsLabelProp, vsLabel, valueSpecIndividual);
		}
		
		createAnnotationAndAddToOntology(rdfsLabelProp, areaTxt, valueSpecIndividual);
		
		//has measurement unit label from value spec to appropriate unit label
		OWLObjectPropertyAssertionAxiom ax6 = factory.getOWLObjectPropertyAssertionAxiom(hasUnitLabel, valueSpecIndividual, unitLabel);
		manager.addAxiom(currentOntology, ax6);
	}
	
	protected void createLatLon(OWLNamedIndividual county, String name, String usps, String lat, String lon) {
		String label3D = "spatial region occupied by region of " + name + ", " + usps;
		String labelPoint = "2015 interior point of spatial region occupied by region of " + name + ", " + usps;
		String labelLat = "latitude measurement of 2015 interior point of region of " + name + ", " + usps;
		String labelLon = "longitude measurement of 2015 interior point of region of " + name + ", " + usps;
		String latVsLabel = lat + " degrees";
		String lonVsLabel = lon + " degrees";
				
		OWLNamedIndividual threeDregion = factory.getOWLNamedIndividual(nextIri());
		OWLNamedIndividual point = factory.getOWLNamedIndividual(nextIri());
		OWLNamedIndividual latM = factory.getOWLNamedIndividual(nextIri());
		OWLAnonymousIndividual latVS = factory.getOWLAnonymousIndividual();
		OWLNamedIndividual lonM = factory.getOWLNamedIndividual(nextIri());
		OWLAnonymousIndividual lonVS = factory.getOWLAnonymousIndividual();
		
		createAnnotationAndAddToOntology(rdfsLabelProp, label3D, threeDregion);
		createAnnotationAndAddToOntology(rdfsLabelProp, labelPoint, point);
		createAnnotationAndAddToOntology(rdfsLabelProp, labelLat, latM);
		createAnnotationAndAddToOntology(rdfsLabelProp, labelLon, lonM);
		createAnnotationAndAddToOntology(rdfsLabelProp, latVsLabel, latVS);
		createAnnotationAndAddToOntology(rdfsLabelProp, lonVsLabel, lonVS);
		
		//the 3d region is instance of 3D region
		OWLClassAssertionAxiom cax1 = factory.getOWLClassAssertionAxiom(threeD, threeDregion);
		
		//the point is instance of 0D region
		OWLClassAssertionAxiom cax2 = factory.getOWLClassAssertionAxiom(zeroD, point);
		
		//the latitude measurement is an instance of latitude measurement
		OWLClassAssertionAxiom cax3 = factory.getOWLClassAssertionAxiom(latMeasurement, latM);
		
		//the longitude measurement is an instance of longitude measurement
		OWLClassAssertionAxiom cax4 = factory.getOWLClassAssertionAxiom(lonMeasurement, lonM);
		
		manager.addAxiom(currentOntology, cax1);
		manager.addAxiom(currentOntology, cax2);
		manager.addAxiom(currentOntology, cax3);
		manager.addAxiom(currentOntology, cax4);
		
		//the geographic region occupies the 3D spatial region
		OWLObjectPropertyAssertionAxiom ax1 = factory.getOWLObjectPropertyAssertionAxiom(occupiesRegion, county, threeDregion);
		//the 3d region has the point as a proper part
		OWLObjectPropertyAssertionAxiom ax2 = factory.getOWLObjectPropertyAssertionAxiom(hasPropPart, threeDregion, point);
		//the latitude measurement is about the point
		OWLObjectPropertyAssertionAxiom ax3 = factory.getOWLObjectPropertyAssertionAxiom(isAbout, latM, point);
		//the longitude measurement is about the point
		OWLObjectPropertyAssertionAxiom ax4 = factory.getOWLObjectPropertyAssertionAxiom(isAbout, lonM, point);
		//the lat measurement has value specification the latVS
		OWLObjectPropertyAssertionAxiom ax5 = factory.getOWLObjectPropertyAssertionAxiom(hasValueSpec, latM, latVS);
		//the long measurement has value specification the lonVS
		OWLObjectPropertyAssertionAxiom ax6 = factory.getOWLObjectPropertyAssertionAxiom(hasValueSpec, lonM, lonVS);
		//the latVS has unit label angular degree
		OWLObjectPropertyAssertionAxiom ax7 = factory.getOWLObjectPropertyAssertionAxiom(hasUnitLabel, latVS, angularDegree);
		//the lonVS has unit label angular degree
		OWLObjectPropertyAssertionAxiom ax8 = factory.getOWLObjectPropertyAssertionAxiom(hasUnitLabel, latVS, angularDegree);
		
		manager.addAxiom(currentOntology, ax1);
		manager.addAxiom(currentOntology, ax2);
		manager.addAxiom(currentOntology, ax3);
		manager.addAxiom(currentOntology, ax4);
		manager.addAxiom(currentOntology, ax5);
		manager.addAxiom(currentOntology, ax6);
		manager.addAxiom(currentOntology, ax7);
		manager.addAxiom(currentOntology, ax8);
		
		//the latVS has value the lat from the file
		OWLDataPropertyAssertionAxiom dax1 = factory.getOWLDataPropertyAssertionAxiom(hasMeasValue, latVS, Double.parseDouble(lat));
		//the lonVS has value the lon from the file
		OWLDataPropertyAssertionAxiom dax2 = factory.getOWLDataPropertyAssertionAxiom(hasMeasValue, lonVS, Double.parseDouble(lon));
		
		manager.addAxiom(currentOntology, dax1);
		manager.addAxiom(currentOntology, dax2);
		
	}

	protected IRI nextIri() {
		String numTxt = Long.toString(iriCounter);
		int numPadZeroes = 9 - numTxt.length();
		char[] chars = new char[numPadZeroes];
		Arrays.fill(chars, '0');
		String pad = new String(chars);
		String iriTxt = "http://purl.obolibrary.org/obo/GEO_" + pad + numTxt;
		iriCounter++;
		return IRI.create(iriTxt);
	}
}
