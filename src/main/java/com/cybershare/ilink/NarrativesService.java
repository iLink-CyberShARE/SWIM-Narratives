package com.cybershare.ilink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.cybershare.ilink.database.entities.Adjective;
import com.cybershare.ilink.database.entities.BaseLine;
import com.cybershare.ilink.database.entities.Element;
import com.cybershare.ilink.database.entities.Explanation;
import com.cybershare.ilink.database.entities.Narrative;
import com.cybershare.ilink.database.entities.Template;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Path("/")
public class NarrativesService 
{
	boolean bCreateDummyTemplate = false;
	boolean testing = false;
	private MongoClient mClient;
	private Morphia mMorphia;
	private Datastore mDataStore;
	private String mSWIMurl = "https://water.cybershare.utep.edu/swim/data/getUserScenario?usid=";
	
	String mongoUser = "";
	String mongoPassword = "";
	String mongoURL = "";
	String mongoPort = "";
	String mongoDBName = "";
		
	@GET
	@Path("data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response requestNarratives(@QueryParam("usid") String userScenarioId,
									@QueryParam("varName") String varName,
									@QueryParam("userType") String userType,
									@QueryParam("focusArea") String focusArea,
									@QueryParam("region") String region,
									@QueryParam("lang") String lang,
									@QueryParam("varValueName") String varValueName,
									@QueryParam("varValueRegion") String varValueRegion){
		String jsonOutput = "";
		if(bCreateDummyTemplate && testing)
			createDummyTemplate();
		else if(!bCreateDummyTemplate){
			Narrative narrative = getNarrative(varName, userType, 
					focusArea, region, lang);
			if(narrative == null)
				return null;
			JsonObject userScenario = getUserScenario(userScenarioId);
			Variable variable = calcStatistics(varName, userScenario, 
					varValueName, varValueRegion, lang, region, narrative);
			narrative = assemblyNarrative(variable, narrative, region, userType);
			jsonOutput = getAsJsonObject(narrative).toString();
		}

		return Response.status(200).entity(jsonOutput).build();
	}
	private Narrative assemblyNarrative(Variable variable, Narrative narrative, 
			String region, String userType) {
		for(String key : variable.getData().keySet())
			for(Template template : narrative.getTemplates())
				template.setText(template.getText()
						.replaceAll(key, variable.getData().get(key)));
		return narrative;
	}
	private JsonObject getAsJsonObject(Narrative narrative) {
		Gson gson = new Gson();
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonTemplate = new JsonObject();

		if(!narrative.isTemplatesEmpty()) {
			List<String> narrativeText = new ArrayList<>();
			for(Template template : narrative.getTemplates())
				narrativeText.add(template.getText());
			jsonTemplate.add("text", 
					(JsonArray) jsonParser.parse(gson.toJson(narrativeText)));
		}
		
		if(!narrative.isExplanationsEmpty()) {
			List<String> narrativeExplanation = new ArrayList<>();
			for(Explanation explanation : narrative.getExplanations())
				narrativeExplanation.add(explanation.getText());
			jsonTemplate.add("explanations", 
					(JsonArray) jsonParser.parse(gson.toJson(narrativeExplanation)));
		}
		return jsonTemplate;
	}
	private Variable calcStatistics(String varName, JsonObject userScenario, 
			String varValueName, String varValueRegion, String lang,
			String region, Narrative narrative) {
		
		String label = "", description = "";
		JsonArray structure = null;
		String structureMain = "", structureName = "", structureLabel = "",
				structureDescription = "", structureInfo = "", structureValue = "",
				structureValuesYear = "", structureValuesValue = "",
				structureNameElementUnit = "";
		boolean isScenario = false, isOutput = false, isInput = false;

		if(narrative.getElement().getTypeElement().equals("swim-terms:Input")) {
			isInput = true;
			structureMain = "modelInputs";
			structureName = "paramName";
			structureLabel = "paramLabel";
			structureDescription = "paramDescription";
			structureInfo = "paraminfo";
			structureValue = "paramValue";
			structureValuesYear = "value";
			structureValuesValue = "value";
		}else if(narrative.getElement().getTypeElement().equals("swim-terms:Output")) {
			isOutput = true;
			structureMain = "modelOutputs";
			structureName = "varName";
			structureLabel = "varLabel";
			structureDescription = "varDescription";
			structureInfo = "varinfo";
			structureValue = "varValue";
			structureValuesYear = "t";
			structureValuesValue = "value";
			structureNameElementUnit = "varUnit";
		}else if(narrative.getElement().getTypeElement().equals("swim-terms:Scenario")) {
			isScenario = true;
			structureMain = "modelSets";
			structureName = "paramName";
			structureLabel = "setValueName";
			structureDescription = "setValueDescription";
			structureInfo = "setinfo";
		}
		structure = userScenario.get(structureMain).getAsJsonArray();
		for(int i = 0; i < structure.size(); i++) {
			JsonObject variable = (JsonObject) structure.get(i);
			
			if(variable.get(structureName).getAsString().replaceAll("\"", "")
					.equals(varName)) {
				
				NavigableMap<String, Float> varValue = new TreeMap<String, Float>();
				JsonArray values = variable.get(structureValue).getAsJsonArray();
				int minYear = 0, maxYear = 0;
				float min = Float.MAX_VALUE;
				float max = Float.MIN_VALUE;
				float average = 0;
				float previousValue = Float.MIN_VALUE;
				float trend = Float.MIN_VALUE;
				int previousValueYear = 0;
				String explanation = "";
				boolean isTrendIncreasing = false;
				String adjectiveTrend = "", adjectiveBehaviour = "";
				String elementUnit = "";
				
				//Get label and description
				if(lang.equals("en")) {
					label = variable.get(structureLabel).getAsString();
					description = variable.get(structureDescription).getAsString();
				}else if(lang.equals("es")) {
					JsonArray info = variable.get(structureInfo).getAsJsonArray();
					label = info.get(0).getAsJsonObject().get(structureLabel).getAsString();
					description = info.get(0).getAsJsonObject().get(structureDescription).getAsString();
				}
				
				//Get explanation
				if(!narrative.isExplanationsEmpty()) {
					//Explanation should be just one element
					explanation = narrative.getExplanations().get(0).getText();
				}
				
				if(!isScenario) {
					for(int j = 0; j < values.size(); j++) {
						if(values.get(j).getAsJsonObject().get(varValueName) != null
							&& values.get(j).getAsJsonObject().get(varValueName).getAsString().equals(varValueRegion)) {
							//Get values
							varValue.put(values.get(j).getAsJsonObject().get(structureValuesYear).getAsString(),
									values.get(j).getAsJsonObject().get(structureValuesValue).getAsFloat());
							//Get min
							if(values.get(j).getAsJsonObject().get(structureValuesValue).getAsInt() < min) {
								min = values.get(j).getAsJsonObject().get(structureValuesValue).getAsFloat();
								minYear = values.get(j).getAsJsonObject().get(structureValuesYear).getAsInt();
							}
							//Get max
							if(values.get(j).getAsJsonObject().get(structureValuesValue).getAsInt() > max) {
								max = values.get(j).getAsJsonObject().get(structureValuesValue).getAsFloat();
								maxYear = values.get(j).getAsJsonObject().get(structureValuesYear).getAsInt();
							}
							//Get average
							average += values.get(j).getAsJsonObject().get(structureValuesValue).getAsFloat();
							//Get constant
							if(values.get(j).getAsJsonObject().get(structureValuesValue).getAsFloat() != previousValue) {
								previousValue = values.get(j).getAsJsonObject().get(structureValuesValue).getAsFloat();
								previousValueYear = values.get(j).getAsJsonObject().get(structureValuesYear).getAsInt();
							}
						}else if(narrative.getElement().getType().equals("average")
								|| varValueName == null || varValueName.equals("")) {
							//This "if" is for historical values only
							//Get values
							varValue.put(String.valueOf(0),
									(float) getHistoricalValue(varName));
							varValue.put(String.valueOf(1),
									values.get(j).getAsJsonObject().get("value").getAsFloat());
							//Get average
							average += values.get(j).getAsJsonObject().get("value").getAsFloat();
						}
					}
					//Get average
					average = average / values.size();
					
					//Get trend
					trend = 100 - (varValue.lastEntry().getValue() * 100 / varValue.firstEntry().getValue());
					if(trend > 0) { //decreasing
						isTrendIncreasing = false;
					}else {
						isTrendIncreasing = true;
						trend = trend * -1;
					}
					
					//Get adjective trend and behaviour
					if(!narrative.isAdjecivesEmpty())
					for(Adjective adjective : narrative.getAdjectives()) {
						if((isTrendIncreasing && adjective.getBehaviour().equals("increasing")) ||
								(!isTrendIncreasing && adjective.getBehaviour().equals("decreasing"))){
							if(adjective.getType().equals("behaviour"))
								adjectiveBehaviour = adjective.getValue();
							else if(adjective.getType().equals("trend"))
								adjectiveTrend = adjective.getValue();
						}
					}
					//Get element unit
					if(isOutput)
						elementUnit = variable.get(structureNameElementUnit).getAsString();
				}
				
				return new Variable(min, minYear, max, maxYear, average, varValue, 
						values.size() > 1 ? true : false, trend, previousValue, 
						previousValueYear, label, description, explanation, 
						isTrendIncreasing, adjectiveTrend, adjectiveBehaviour,
						region, elementUnit, getHistoricalValue(varName));
			}
		}
		return null;
	}
	
	/**
	 * Get historical value from MongoDB associated to a varName
	 * @param varName 
	 * @return
	 */
	private double getHistoricalValue(String varName) {
		// TODO Auto-generated method stub
		openConnection();
		List<BaseLine> baseLine = mDataStore.createQuery(BaseLine.class)
                .filter("name", varName)
                .asList();
		closeConnection();
		if(baseLine.isEmpty())
			return 0;
		return baseLine.get(0).getValue();
	}
	private Narrative getNarrative(String varName, String userType,
			String focusArea, String region, String lang) {

		openConnection();
		List<Narrative> inputNarrative = mDataStore.createQuery(Narrative.class)
                .filter("element.name", varName)
                .asList();
		closeConnection();
		if(inputNarrative.isEmpty())
			return null;
		Narrative narrative = inputNarrative.get(0);
		
		//Remove templates that do not meet critera
		if(!narrative.isTemplatesEmpty()) {
			List<Template> templatesToRemove = new ArrayList<>();
			for(Template template : narrative.getTemplates()) {
				if(template.getLang().equals(lang) &&
						template.getFocusArea().contains(focusArea) &&
						template.getUserType().contains(userType) &&
						template.getRegion().contains(region))
					continue;
				templatesToRemove.add(template);
			}
			narrative.getTemplates().removeAll(templatesToRemove);
		}

		//Remove adjectives that do not meet criteria
		if(!narrative.isAdjecivesEmpty()) {
			List<Adjective> adjectivesToRemove = new ArrayList<>();
			for(Adjective adjective : narrative.getAdjectives()) {
				if(adjective.getLang().equals(lang))
					continue;
				adjectivesToRemove.add(adjective);
			}
			narrative.getAdjectives().removeAll(adjectivesToRemove);
		}

		//Remove explanations that do not meet criteria
		if(!narrative.isExplanationsEmpty()) {
			List<Explanation> explanationsToRemove = new ArrayList<>();
			for(Explanation explanation : narrative.getExplanations()) {
				if(explanation.getLang().equals(lang) &&
						explanation.getUserType().contains(userType))
					continue;
				explanationsToRemove.add(explanation);
			}
			narrative.getExplanations().removeAll(explanationsToRemove);
		}
		
		return narrative;
	}
	private JsonObject getUserScenario(String user_id) {
		try {
		Client client = Client.create();

		WebResource webResource = client
		   .resource(mSWIMurl  + user_id);

		ClientResponse response = webResource.accept("application/json")
                   .get(ClientResponse.class);

		if (response.getStatus() != 200) {
		   throw new RuntimeException("Failed : HTTP error code : "
			+ response.getStatus());
		}

		String output = response.getEntity(String.class);

		output = output.replace("<pre>", "").replace("</pre>", "");
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject) jsonParser.parse(output);
		return jsonObject;
	  } catch (Exception e) {

		e.printStackTrace();

	  }
		return null;
	}
	/**
	 * Open a connection to MongoDB
	 */
	private void openConnection() {
		if(testing)
		{
			mClient = new MongoClient("localhost", 27017);
		}else {
			MongoClientURI mongoUri = new MongoClientURI("mongodb://" + mongoUser + ":" + mongoPassword + "@" + mongoURL + ":" + mongoPort);
			mClient = new MongoClient(mongoUri);
		}
		mMorphia = new Morphia();
		mDataStore = mMorphia.createDatastore(mClient, mongoDBName);

		mMorphia.map(Narrative.class);
		mMorphia.map(Element.class);
		mMorphia.map(Template.class);
		mMorphia.map(Explanation.class);
		mMorphia.map(BaseLine.class);
		mDataStore.ensureIndexes();
	}
	/**
	 * Close connection to MongoDB
	 */
	private void closeConnection() {
		mClient.close();
	}
	
	/**
	 * Method for creating a new JSON-LD inside MongoDB
	 */
	private void createDummyTemplate() {
		Narrative narrativeWaterStocks = new Narrative();
		Element elementWaterStocks = new Element();
		List<Template> listTemplatesWaterStocks = new ArrayList<>();
		List<Adjective> listAdjectivesWaterStocks = new ArrayList<>();

		//General info
		narrativeWaterStocks.setContext("http://purl.org/swim/vocab");
		narrativeWaterStocks.setType("swim-terms:TemplateInventory");
		//Element structure
		elementWaterStocks = buildElement("swim-terms:Output",
				"water_stocks","output");
		narrativeWaterStocks.setElement(elementWaterStocks);
		//Template structure


		listTemplatesWaterStocks.add(buildTemplate("swim-terms:Template",
				"~element_label follows a ~adjective_trend trend with a "
						+ "~percent ~adjective_behaviour of water "
						+ "stocks by the end year with a peak "
						+ "volume of ~maxValue KAF in ~maxYear and "
						+ "the lowest volume of ~minValue KAF "
						+ "in ~minYear.",Arrays.asList("citizen"),
						Arrays.asList("urban"),
						Arrays.asList("El Paso"),"en"));
		listTemplatesWaterStocks.add(buildTemplate("swim-terms:Template",
				"Almacenaminto en el caballo y en elephant booth (EB+Caballo) "
						+ "sigue un ~adjective_trend con un porcentaje "
						+ "~percent ~adjective_behaviour de agua "
						+ "con un máximo "
						+ "volumen de ~maxValue KAF en ~maxYear y "
						+ "el volumen más bajo de ~minValue KAF "
						+ "en ~minYear.",Arrays.asList("citizen"),
						Arrays.asList("urban"),
						Arrays.asList("El Paso"),"es"));
		narrativeWaterStocks.setTemplates(listTemplatesWaterStocks);
		//Adjectives structure
		listAdjectivesWaterStocks.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "reduction", "en", "decreasing"));
		listAdjectivesWaterStocks.add(buildAdjective("swim-terms:Adjective",
				"trend", "downward", "en", "decreasing"));
		listAdjectivesWaterStocks.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "increase", "en", "increasing"));
		listAdjectivesWaterStocks.add(buildAdjective("swim-terms:Adjective",
				"trend", "upward", "en", "increasing"));

		listAdjectivesWaterStocks.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "reducción", "es", "decreasing"));
		listAdjectivesWaterStocks.add(buildAdjective("swim-terms:Adjective",
				"trend", "decremento", "es", "decreasing"));
		listAdjectivesWaterStocks.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "aumento", "es", "increasing"));
		listAdjectivesWaterStocks.add(buildAdjective("swim-terms:Adjective",
				"trend", "incremento", "es", "increasing"));

		narrativeWaterStocks.setAdjectives(listAdjectivesWaterStocks);
		

		Narrative narrativeBigStressScenario = new Narrative();
		Element elementBigStressScenario = new Element();
		List<Template> listTemplatesBigStressScenario = new ArrayList<>();
		List<Explanation> listExplanationsBigStressScenario = new ArrayList<>();

		//General info
		narrativeBigStressScenario.setContext("http://purl.org/swim/vocab");
		narrativeBigStressScenario.setType("swim-terms:TemplateInventory");
		//Element structure
		elementBigStressScenario = buildElement("swim-terms:Scenario",
				"big_stress_scenario","scenario");
		narrativeBigStressScenario.setElement(elementBigStressScenario);
		//Template structure
		listTemplatesBigStressScenario.add(buildTemplate("swim-terms:Template",
				"The selected water supply of ~element_label "
				+ "Scenario appends ~element_description "
				+ "~explanation.", Arrays.asList("General Public",
						"Water Admninistrator", "Scientific"),
				Arrays.asList("urban","agriculture"),
				Arrays.asList("El Paso" , "Las Cruces",
						"Ciudad Juarez"),"en"));
		listTemplatesBigStressScenario.add(buildTemplate("swim-terms:Template",
				"El suministro de agua seleccionado de "
				+ "~element_label adjunta afluencias de "
				+ "~element_description ~explanation.",
				Arrays.asList("General Public", 
						"Water Admninistrator", "Scientific"),
				Arrays.asList("urban","agriculture"),
				Arrays.asList("El Paso" , "Las Cruces",
						"Ciudad Juarez"),"es"));
		narrativeBigStressScenario.setTemplates(listTemplatesBigStressScenario);
		//Explanation structure
		listExplanationsBigStressScenario.add(buildExplanation("swim-terms:Explanation",
				"This hydroclimate simulation was generated by "
				+ "the US Bureau of Reclamation as part of its "
				+ "West Wide Climate Risk Assessment, then "
				+ "modified by the SWIM team to generate extended "
				+ "drought conditions for San Marcial flows after "
				+ "2013.", Arrays.asList("Scientific"),"en"));
		listExplanationsBigStressScenario.add(buildExplanation("swim-terms:Explanation",
				"Extends drought conditions in the region after "
				+ "2013 over inflows at San Marcial derived from "
				+ "the HadGEM2 simulation based on climate and "
				+ "hydrology.", Arrays.asList("General Public", 
						"Water Admninistrator"),"en"));
		listExplanationsBigStressScenario.add(buildExplanation("swim-terms:Explanation",
				"Esta simulación hidroclimática, fue generada "
				+ "por el US Bureau of Reclamation como parte "
				+ "de su Evaluación de Riesgo para el Ancho del "
				+ "Oeste, luego modificada por el equipo de SWIM "
				+ "para generar condiciones extendidas de sequía "
				+ "para flujos de entrada en San Marcial después "
				+ "del 2013.", Arrays.asList("Scientific"),"es"));
		listExplanationsBigStressScenario.add(buildExplanation("swim-terms:Explanation",
				"Extiende condiciones de sequía en la región "
				+ "después del 2013 con flujos de entrada en "
				+ "San Marcial derivados de la simulación HadGEM2 "
				+ "que se basa en clima e hidrologia.", 
				Arrays.asList("General Public", 
						"Water Admninistrator"),"es"));
		narrativeBigStressScenario.setExplanations(listExplanationsBigStressScenario);
		
		Narrative narrativeUrbanPrice = new Narrative();
		Element elementUrbanPrice = new Element();
		List<Template> listTemplatesUrbanPrice = new ArrayList<>();
		List<Adjective> listAdjectivesUrbanPrice = new ArrayList<>();

		//General info
		narrativeUrbanPrice.setContext("http://purl.org/swim/vocab");
		narrativeUrbanPrice.setType("swim-terms:TemplateInventory");
		//Element structure
		elementUrbanPrice = buildElement("swim-terms:Output",
				"urb_price","output");
		narrativeUrbanPrice.setElement(elementUrbanPrice);
		//Template structure
		listTemplatesUrbanPrice.add(buildTemplate("swim-terms:Template",
				"Urban Prices in ~region follow a ~adjective_behaviour ~adjective_trend "
				+ "trend that peaks and remains constant as of ~constant_year "
				+ "at ~constant_value USD/AF, lowest value is in ~minValue "
				+ "USD/AF in ~minYear.",Arrays.asList("all","other"),
						Arrays.asList("urban"),
						Arrays.asList("El Paso"),"en"));
		listTemplatesUrbanPrice.add(buildTemplate("swim-terms:Template",
				"Los precios urbanos en ~region siguen un ~adjective_behaviour "
				+ "~adjective_trend que tiene como máximo y permanece constante "
				+ "desde el año ~constant_year en ~constant_value USD/AF, "
				+ "el valor más bajo es en ~minValue USD/AF en ~minYear.",
				Arrays.asList("all","other"),
						Arrays.asList("urban"),
						Arrays.asList("El Paso"),"es"));
		narrativeUrbanPrice.setTemplates(listTemplatesUrbanPrice);
		//Adjectives structure
		listAdjectivesUrbanPrice.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "reduction", "en", "decreasing"));
		listAdjectivesUrbanPrice.add(buildAdjective("swim-terms:Adjective",
				"trend", "downward", "en", "decreasing"));
		listAdjectivesUrbanPrice.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "increase", "en", "increasing"));
		listAdjectivesUrbanPrice.add(buildAdjective("swim-terms:Adjective",
				"trend", "upward", "en", "increasing"));

		listAdjectivesUrbanPrice.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "reducción", "es", "decreasing"));
		listAdjectivesUrbanPrice.add(buildAdjective("swim-terms:Adjective",
				"trend", "decremento", "es", "decreasing"));
		listAdjectivesUrbanPrice.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "aumento", "es", "increasing"));
		listAdjectivesUrbanPrice.add(buildAdjective("swim-terms:Adjective",
				"trend", "incremento", "es", "increasing"));
		narrativeUrbanPrice.setAdjectives(listAdjectivesUrbanPrice);
		
		Narrative narrativeAvgInflows = new Narrative();
		Element elementAvgInflows = new Element();
		List<Template> listTemplatesAvgInflows = new ArrayList<>();
		List<Adjective> listAdjectivesAvgInflows = new ArrayList<>();

		//General info
		narrativeAvgInflows.setContext("http://purl.org/swim/vocab");
		narrativeAvgInflows.setType("swim-terms:TemplateInventory");
		//Element structure
		elementAvgInflows = buildElement("swim-terms:Output",
				"avginflows","output");
		narrativeAvgInflows.setElement(elementAvgInflows);
		//Template structure
		listTemplatesAvgInflows.add(buildTemplate("swim-terms:Template",
				"Water Supply outcome causes a ~adjective_trend ~adjective_behaviour in "
				+ "average inflows to Elephant Butte in comparison to the historical "
				+ "period (1995-2015). Historical averages were ~historical_avg ~element_unit and "
				+ "the selected scenario results in ~average ~element_unit average "
				+ "annual inflow.",Arrays.asList("all","other"),
						Arrays.asList("urban"),
						Arrays.asList("El Paso"),"en"));
		listTemplatesAvgInflows.add(buildTemplate("swim-terms:Template",
				"El valor de el ingreso de agua cause un ~adjective_trend "
				+ "~adjective_behaviour en promedio a Elephant Butte en comparasión "
				+ "con el periodo histórico (1995-2015). El promedio histórico es "
				+ "de ~historical_avg element_unit y el escenario configurado resulta en un promedio "
				+ "anual de ingreso de ~average ~element_unit",
				Arrays.asList("all","other"),
						Arrays.asList("urban"),
						Arrays.asList("El Paso"),"es"));
		narrativeAvgInflows.setTemplates(listTemplatesAvgInflows);
		//Adjectives structure
		listAdjectivesAvgInflows.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "reduction", "en", "decreasing"));
		listAdjectivesAvgInflows.add(buildAdjective("swim-terms:Adjective",
				"trend", "downward", "en", "decreasing"));
		listAdjectivesAvgInflows.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "increase", "en", "increasing"));
		listAdjectivesAvgInflows.add(buildAdjective("swim-terms:Adjective",
				"trend", "upward", "en", "increasing"));

		listAdjectivesAvgInflows.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "reducción", "es", "decreasing"));
		listAdjectivesAvgInflows.add(buildAdjective("swim-terms:Adjective",
				"trend", "decremento", "es", "decreasing"));
		listAdjectivesAvgInflows.add(buildAdjective("swim-terms:Adjective",
				"behaviour", "aumento", "es", "increasing"));
		listAdjectivesAvgInflows.add(buildAdjective("swim-terms:Adjective",
				"trend", "incremento", "es", "increasing"));
		narrativeAvgInflows.setAdjectives(listAdjectivesAvgInflows);
		
		//Building BaseLine
		List<BaseLine> baseLineList = new ArrayList<>();
		baseLineList.add(buildBaseLine("Average Total Inflows", "Summary",
				"avginflows", 582.86, "Average annual total inflows in thousands of acre feet", "KAF/yr"));
		baseLineList.add(buildBaseLine("Rio Grande Project Surface Deliveries",
				"Summary", "avgwaterdeliveries", 426.27, "Average annual surface deliveries excluding first year over selected period in thousands of acre feet", "KAF/yr"));
		baseLineList.add(buildBaseLine("Total Basin Surface Water Diversions",
				"Summary", "avgswdivertions", 412.27, "Average surface water diversions exluding first year over selected period", "KAF/yr"));
		baseLineList.add(buildBaseLine("Total Basin Surface Water Use", "Summary",
				"avgswuse", 174.57, "Average surface water use exluding first year over selected period", "KAF/yr"));
		baseLineList.add(buildBaseLine("Basin Surface Flows", "Summary",
				"avgriverflows", 211.08, "Average annual surface flows in thousands of acre feet", "KAF/yr"));
		baseLineList.add(buildBaseLine("Agricultural Ground Water Pumping", "Summary",
				"avgagpumping", 81.34, "Average annual agricultural water pumping in thousands of acre feet over all three districts", "KAF/yr"));
		baseLineList.add(buildBaseLine("Urban Groundwater Pumping", "Summary",
				"avgurbpumping", 178.31, "Average urban water pumping excluding first year over selected period in thousands of acre feet", "KAF/yr"));
		baseLineList.add(buildBaseLine("Urban Substitute Technology Use", "Summary",
				"avgurbbackuse", 19.1, "Average urban water substitute technology use over selected period", "KAF/yr"));
		baseLineList.add(buildBaseLine("US-Mexico Treaty Deliveries", "Summary",
				"avgmxflows", 59.46, "Average US-MX treaty delivery flows excluding first year over selected period", "KAF/yr"));
		baseLineList.add(buildBaseLine("Farm Income", "Summary", "avgtotagben",
				114379.6, "Average annual agricultural economic benefits in $1000s summed over all three districts", "KUSD/yr"));
		baseLineList.add(buildBaseLine("Reservoir Recreation Economic Benefits",
				"Summary", "avgrecben", 6218.7, "Average annual reservoir recreation economic benefits over selected period in $1000s", "KUSD/yr"));
		baseLineList.add(buildBaseLine("Urban Economic Benefits", "Summary",
				"avgtoturbben", 1389148.01, "Average urban economic benefits excluding first year over selected period", "KUSD/yr"));
		baseLineList.add(buildBaseLine("Average Reservoir Storage", "Summary",
				"avgstocks", 1974.61, "Average project storage in thousands of acre feet summed over Elephant Butte & Caballo", "KAF/yr"));
		baseLineList.add(buildBaseLine("Total Economic Benefits", "Summary",
				"avgtotben", 1511014.06, "Average annual economic benefits over selected period", "KUSD/yr"));

		
		openConnection();
		mDataStore.save(narrativeAvgInflows);
		mDataStore.save(narrativeWaterStocks);
		mDataStore.save(narrativeUrbanPrice);
		mDataStore.save(narrativeBigStressScenario);
		//Saving baseline
		for(BaseLine baseLine : baseLineList)
			mDataStore.save(baseLine);
		closeConnection();
	}

	private BaseLine buildBaseLine(String label, String category, String name,
			double value, String description, String unit) {
		BaseLine baseLine = new BaseLine();
		baseLine.setLabel(label);
		baseLine.setCategory(category);
		baseLine.setName(name);
		baseLine.setValue(value);
		baseLine.setDescription(description);
		baseLine.setUnit(unit);
		return baseLine;
	}
	/**
	 * Builds an adjective
	 * @param typeAdjective
	 * @param type
	 * @param value
	 * @param lang
	 * @param behaviour 
	 * @return
	 */
	private Adjective buildAdjective(String typeAdjective, 
			String type, String value, String lang, String behaviour) {
		Adjective adjective = new Adjective();
		adjective.setTypeAdjective(typeAdjective);
		adjective.setType(type);
		adjective.setValue(value);
		adjective.setLang(lang);
		adjective.setBehaviour(behaviour);
		return adjective;
	}
	private Explanation buildExplanation(String typeExplanation, 
			String text, List<String> userType, String lang) {
		Explanation explanation = new Explanation();
		explanation.setType(typeExplanation);
		explanation.setText(text);
		explanation.setUserType(userType);
		explanation.setLang(lang);
		return explanation;
	}
	private Template buildTemplate(String type, String text, 
			List<String> userType, List<String> focusArea,
			List<String> region, String lang) {
		Template template = new Template();
		template.setType(type);
		template.setText(text);
		template.setUserType(userType);
		template.setFocusArea(focusArea);
		template.setRegion(region);
		template.setLang(lang);
		return template;
	}
	/**
	 * Builds an Element structure
	 * @param @type
	 * @param name
	 * @param type
	 * @return Element
	 */
	private Element buildElement(String typeElement, String name, String type) {
		Element element = new Element();
		element.setTypeElement(typeElement);
		element.setName(name);
		element.setType(type);
		return element;
	}
}