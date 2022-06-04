package helio.builder.rmlmapper;

import java.io.ByteArrayInputStream;
import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import helio.blueprints.TranslationUnit;
import helio.blueprints.UnitBuilder;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.exceptions.TranslationUnitExecutionException;

public class RmlUnitBuilder implements UnitBuilder {

	@Override
	public void configure(JsonObject configuration) {

	}

	@Override
	public Set<TranslationUnit> parseMapping(String content) throws IncompatibleMappingException,
			TranslationUnitExecutionException, IncorrectMappingException, ExtensionNotFoundException {

		try {
			Model results = Rio.parse(new ByteArrayInputStream(content.getBytes()), "http://https://helio-ecosystem.github.io/helio-builder-rmlmapper", RDFFormat.TURTLE);
			results.clear();
		} catch (Exception e) {
			throw new IncorrectMappingException(e.toString());
		}
		return Sets.newHashSet(new RmlTranslationUnit(content));
	}

}
