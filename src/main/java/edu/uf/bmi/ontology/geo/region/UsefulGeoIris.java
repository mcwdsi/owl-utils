package edu.uf.bmi.ontology.geo.region;

import org.semanticweb.owlapi.model.IRI;

public interface UsefulGeoIris {
	String rdfsLabelIriText = "http://www.w3.org/2000/01/rdf-schema#label";
	String iso31661alpha_3_codeIriText = "http://purl.obolibrary.org/obo/GEO_000000022";
	String iso31661alpha_2_codeIriText = "http://purl.obolibrary.org/obo/GEO_000000023";
	String uNNumericalCodeIriText = "http://purl.obolibrary.org/obo/GEO_000000021";
	String nationClassIriText = "http://purl.obolibrary.org/obo/GEO_000000396";
	String geoRegionIriText = "http://purl.obolibrary.org/obo/GEO_000000372";
	String geoDependencyIriText = "http://purl.obolibrary.org/obo/GEO_000000006";
	
	IRI nationClassIri = IRI.create(nationClassIriText);
	IRI rdfsLabelIri = IRI.create(rdfsLabelIriText);
	IRI iso31661alpha_3_codeIri = IRI.create(iso31661alpha_3_codeIriText);
	IRI iso31661alpha_2_codeIri = IRI.create(iso31661alpha_2_codeIriText);
	IRI uNNumericalCodeIri = IRI.create(uNNumericalCodeIriText);
	IRI geoRegionIri = IRI.create(geoRegionIriText);
	IRI geoDependencyIri = IRI.create(geoDependencyIriText);
}
