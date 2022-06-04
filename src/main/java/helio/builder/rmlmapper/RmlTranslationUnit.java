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
import helio.blueprints.exceptions.TranslationUnitExecutionException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.JsonObject;

public class RmlTranslationUnit implements TranslationUnit{

	private static final String CODE_EXCEPTION = "translation_exception_internal_error-AAABBB";
	private String mapping;
	private Integer scheduledTime;
	private UnitType type;
	private String uuid;
	private List<String> translations = new CopyOnWriteArrayList<>();
	private StringBuilder builder = new StringBuilder();
	
	public RmlTranslationUnit(String mapping) {
		this.mapping = mapping;
		this.type = UnitType.Sync;
		this.uuid = UUID.randomUUID().toString();
	}
	
	@Override
	public Runnable getTask() throws TranslationUnitExecutionException {
		return new Runnable() {

			@Override
			public void run() {
				String result = translate();
				if(result!=null)
					translations.add(result);
			}
			
			private String translate() {
				try {
		            InputStream mappingStream = new ByteArrayInputStream(mapping.getBytes());
		            QuadStore rmlStore = QuadStoreFactory.read(mappingStream);

		            // Set up the basepath for the records factory, i.e., the basepath for the (local file) data sources
		            RecordsFactory factory = new RecordsFactory(".");
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
		        	builder.append(e.toString());
		        	return null;
		        }
			}
		};
	}

	@Override
	public List<String> getDataTranslated() throws TranslationUnitExecutionException {
		if(!builder.isEmpty()) {
			String msg = builder.toString();
			builder = new StringBuilder();
			throw new TranslationUnitExecutionException(msg);
			
		}
		List<String> translations = new ArrayList<>();
		translations.addAll(this.translations);
		this.translations.clear();
		return translations;
	}

	@Override
	public void flushDataTranslated() throws TranslationUnitExecutionException {
		// empty
		translations.clear();
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
