package helio.builder.rmlmapper;

import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import helio.blueprints.TranslationUnit;
import helio.blueprints.UnitType;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.exceptions.TranslationUnitExecutionException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import com.google.gson.JsonObject;

public class RmlTranslationUnit implements TranslationUnit{

	private String mapping;
	private Integer scheduledTime;
	private UnitType type;
	private String uuid;
	
	public RmlTranslationUnit(String mapping) {
		this.mapping = mapping;
		this.type = UnitType.Sync;
		this.uuid = UUID.randomUUID().toString();
	}
	
	@Override
	public Callable<String> getTask(Map<String, Object> arguments) throws TranslationUnitExecutionException {
		return new Callable<String>() {
			
			private String translate() throws IncorrectMappingException {
				try {
		            InputStream mappingStream = new ByteArrayInputStream(mapping.getBytes());
		            QuadStore rmlStore = QuadStoreFactory.read(mappingStream);

		            // Set up the basepath for the records factory, i.e., the basepath for the (local file) data sources
		            RecordsFactory factory = new RecordsFactory("./");
		            @SuppressWarnings("rawtypes")
					Map<String, Class> libraryMap = new HashMap<>();
		            libraryMap.put("IDLabFunctions", IDLabFunctions.class);
		            FunctionLoader functionLoader = new FunctionLoader(null, libraryMap);
		            QuadStore outputStore = new RDF4JStore();
		            Executor executor = new Executor(rmlStore, factory, functionLoader, outputStore, Utils.getBaseDirectiveTurtle(mappingStream));
		            QuadStore result = executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));
		            Writer writer = new StringWriter();
		            result.write(writer, "turtle");
		            return writer.toString();
		        } catch (Exception e) {
		        	throw new IncorrectMappingException(e.toString());
		        }
			}

			@Override
			public String call() throws IncorrectMappingException {
				return translate();
			}
		};
	}

	
	
	

	@Override
	public void configure(JsonObject configuration) {
		// empty
		
	}

	@Override
	public UnitType getUnitType() {
		return this.type;
	}

	@Override
	public void setUnitType(UnitType type) {
		this.type = type;
	}

	@Override
	public String getId() {
		return uuid;
	}

	@Override
	public int getScheduledTime() {
		return scheduledTime;
	}

	@Override
	public void setScheduledTime(int scheduledTime) {
		this.scheduledTime = scheduledTime;
		if(this.scheduledTime==null)
			type=UnitType.Sync;
		type = UnitType.Scheduled;
		
	}
	
}
