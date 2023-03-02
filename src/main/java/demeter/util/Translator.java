/* Copyright 2019-2022 Universidad Politécnica de Madrid (UPM).
 *
 * Authors:
 *    Mario San Emeterio de la Parte
 *    Vicente Hernández Díaz
 *
 * This software is distributed under a dual-license scheme:
 *
 * - For academic uses: Licensed under GNU Affero General Public License as
 *                      published by the Free Software Foundation, either
 *                      version 3 of the License, or (at your option) any
 *                      later version.
 *
 * - For any other use: Licensed under the Apache License, Version 2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * You can get a copy of the license terms in licenses/LICENSE.
 *
 */

package demeter.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import demeter.config.Constants;

public class Translator {

	public static JsonObject translateToAIM(JsonObject dqResponse, String language) {
		System.out.println("Translating..");
		JsonObject baseResponse =  dqResponse.getAsJsonObject("results");
		JsonObject responseAIM = new JsonObject();
		JsonArray resources = new JsonArray();
//		System.out.println(baseResponse.toString());
		if(baseResponse.toString().isEmpty() || !baseResponse.toString().contains("resources")) {
			JsonObject responseNoResults = new JsonObject();
			responseNoResults.addProperty("@graph", "No results found according to the query made");
			return responseNoResults;
		}
		for(JsonElement resource : baseResponse.getAsJsonArray("resources")) {
//			responseAIM.add(resource);
			JsonObject resourceAIM = new JsonObject();
			JsonArray measurementsAIM = new JsonArray();
			for(JsonElement measure : resource.getAsJsonObject().getAsJsonArray("measurements")) {
				String measurement = measure.getAsJsonObject().get("measurement").getAsString().replace("_", " ");
				JsonObject measurementAIM = new JsonObject();
				measurementAIM.addProperty("@type", "Observation");
				measurementAIM.addProperty("observedProperty", measurement);
				JsonArray observations = new JsonArray();
				for(JsonElement observation : measure.getAsJsonObject().get("observations").getAsJsonArray()) {
					JsonObject observationAIM = new JsonObject();
					if(language.equalsIgnoreCase(Constants.EN)) {
						observationAIM.addProperty("Measurement", measurement);
						observationAIM.addProperty("Date", observation.getAsJsonObject().get("time").getAsString());
						observationAIM.addProperty("asWKT", "POINT("+observation.getAsJsonObject().get("longitude").getAsString()+" "+observation.getAsJsonObject().get("latitude").getAsString()+")");
						observationAIM.addProperty("Value", observation.getAsJsonObject().get("value").getAsString());
						observationAIM.addProperty("uom", getMeasurementUnity(observation.getAsJsonObject().get("uom").getAsString()));
						observationAIM.addProperty("Provider", observation.getAsJsonObject().get("provider").getAsString());
					}else if(language.equalsIgnoreCase(Constants.ES)) {
						observationAIM.addProperty("Medida", measurementTranslator(measurement, Constants.ES));
						observationAIM.addProperty("Fecha", observation.getAsJsonObject().get("time").getAsString());
						observationAIM.addProperty("asWKT", "POINT("+observation.getAsJsonObject().get("longitude").getAsString()+" "+observation.getAsJsonObject().get("latitude").getAsString()+")");
						observationAIM.addProperty("Valor", observation.getAsJsonObject().get("value").getAsString());
						observationAIM.addProperty("Unidad", getMeasurementUnity(observation.getAsJsonObject().get("uom").getAsString()));
						observationAIM.addProperty("Proveedor", observation.getAsJsonObject().get("provider").getAsString());
					}
					observations.add(observationAIM);
				}
				measurementAIM.add("containsPlot", observations);
				measurementsAIM.add(measurementAIM);
			}
			resourceAIM.addProperty("@id", resource.getAsJsonObject().get("resource").getAsString().replace("_", " "));
			resourceAIM.add("measurements", measurementsAIM);
			resources.add(resourceAIM);
		}
		
		responseAIM.add("@graph", resources);
		
		return responseAIM;
	}
	
	private static String measurementTranslator(String measurement, String language) {
		if(language.equalsIgnoreCase(Constants.ES)) {
			if(measurement.contains("volumetric")) return "contenido volumétrico de agua del suelo mineral";
			else if(measurement.contains("temperature teros21")) return "temperatura del suelo - sensor teros21";
			else if(measurement.contains("temperature teros12")) return "temperatura del suelo - sensor teros12";
			else if(measurement.contains("soil matrix potential")) return "potencial de la matriz del suelo";
			else if(measurement.contains("relative dielectric constant")) return "constante dieléctrica relativa";
			else if(measurement.contains("electrical conductivity pores")) return "conductividad eléctrica de poros";
			else if(measurement.contains("electrical conductivity bulk")) return "conductividad eléctrica a granel";
			else if(measurement.contains("battery")) return "batería";
			else if(measurement.contains("air temperature")) return "temperatura del aire";
			else if(measurement.contains("air humidity")) return "humedad del aire";
			else if(measurement.contains("wind speed")) return "velocidad del viento";
			else if(measurement.contains("wind direction")) return "dirección del viento";
			else if(measurement.contains("rainfall")) return "pluviometría (precipitaciones)";
			else if(measurement.contains("air preassure")) return "presión del aire";
			else return measurement;
		}else {
			return measurement;
		}
		
	}
	
	private static String getMeasurementUnity(String uom) {
		if(uom.contains("PERCENT")) return "%";
		else if(uom.contains("DEG_C")) return "°C";
		else if(uom.contains("UNITLESS")) return " ";
		else if(uom.contains("S-PER-M")) return "S/m";
		else if(uom.contains("V")) return "V";
		else if(uom.contains("KiloPA")) return "kPa";
		else if(uom.contains("DEG")) return "°";
		else return "";
	}
}
