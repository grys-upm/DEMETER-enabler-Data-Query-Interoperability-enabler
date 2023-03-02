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

package demeter.demeterTranslator;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;

import com.google.gson.JsonObject;

import demeter.config.Constants;
import demeter.rest.GetObservationsFromAFarCloud;
import demeter.util.Translator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Path("/getObservationsBySensor/")
public class GetObservationsBySensor {

	private static final Logger log = Logger.getLogger(GetObservationsBySensor.class);
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return String that will be returned as a application/json response.
     * @throws com.github.fge.jsonschema.core.exceptions.ProcessingException 
     */
	
	private String getRemoteAddress(Request request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");  
		   if (ipAddress == null) {  
		       ipAddress = request.getRemoteAddr();  
		   } 
		   return ipAddress;
	}
    
    @GET
    @Path("/latest/")
    @Produces({ "application/json" })
    @Operation(summary = "Retrieves the latest telemetry from sensors that meet specific constraint", description = "", tags={ "Query sensor telemetry" })
    @ApiResponses(value = { 
    		@ApiResponse(responseCode = "200", description = "Successful Operation"),
    		@ApiResponse(responseCode = "205", description = "Invalid input"),    		
    		@ApiResponse(responseCode = "5XX", description = "Unexpected error")
    		})
    
    public Response getLatestObservationsBySensor(    			
    			@Parameter(in = ParameterIn.QUERY, description = "Number of telemetries to retrieve by sensor. Default 1, maximum defined by services provider.", required = true )
    				@QueryParam(Constants.SRV_PARAM_LIMIT) int nLimit,
    		
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of sensor identifier (entityName) to retrieve." , required = false)
    				@QueryParam(Constants.SRV_PARAM_ENTITY_NAMES) String sEntityNames,    			
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of device identifier (device entityName) to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_DEVICES) String sDevices,
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of application domain to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_SERVICES) String sServices,
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of the type of measurements to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_TYPES) String sTypes,    				
      			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of providers to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_PROVIDERS) String sProviders,
    				
    			@Parameter(in = ParameterIn.QUERY, description = "List (comma separated values) of the measurements (observedProperty) to retrieve." , required = false) 
    				@QueryParam(Constants.SRV_PARAM_MEASUREMENTS) String sMeasurements,

       			@Parameter(in = ParameterIn.QUERY, description = "Minimum altitude of observations.", required = true )
    				@QueryParam(Constants.SRV_PARAM_ALTITUDE) int nAltitude,
    				
        		@Parameter(in = ParameterIn.QUERY, description = "Temporal order: ascending (ASC) or descending (DESC). Default value: DESC.", required = false )
    				@QueryParam(Constants.SRV_PARAM_ORDER) String sOrder,

    			// proximity parameter
           		@Parameter(in = ParameterIn.QUERY, description = "Proximity parameter - Centroid longitude: longitude of the centroid of the search area. To compute proximity based search, all proximity parameters are mandatories", required = false )
        				@QueryParam(Constants.SRV_PARAM_CENTROID_LONGITUDE) double lCentroide_long,
        		@Parameter(in = ParameterIn.QUERY, description = "Proximity parameter - Centroid latitude: latitude of the centroid of the search area. To compute proximity based search, all proximity parameters are mandatories", required = false )
        				@QueryParam(Constants.SRV_PARAM_CENTROID_LATITUDE) double lCentroide_lat,        		
        		@Parameter(in = ParameterIn.QUERY, description = "Proximity parameter - Search radius expressed in meters. Maximun 1000 meters. To compute proximity based search, all proximity parameters are mandatories", required = false )
        				@QueryParam(Constants.SRV_PARAM_RADIUS) int nRadius,    
        				
				@Parameter(in = ParameterIn.QUERY, description = "Response language", required = false )
        				@QueryParam(Constants.SRV_PARAM_LANGUAGE) String language,
        				
    			@Context UriInfo uriInfo, @Context Request request
    			) throws NotFoundException, SocketTimeoutException, IOException {    
    	
    		JsonObject jsonDQResponse = new JsonObject();
    		JsonObject jsonAIMResponse = new JsonObject();
    		String queryToDQ = Constants.DQ_URL+nLimit+"&scenarios="+Constants.fct;
    		
    		if(sEntityNames!=null)queryToDQ+="&"+Constants.SRV_PARAM_ENTITY_NAMES+"="+sEntityNames;
    		if(sDevices!=null)queryToDQ+="&"+Constants.SRV_PARAM_DEVICES+"="+sDevices;
    		if(sServices!=null)queryToDQ+="&"+Constants.SRV_PARAM_SERVICES+"="+sServices;
    		if(sTypes!=null)queryToDQ+="&"+Constants.SRV_PARAM_TYPES+"="+sTypes;
    		if(sProviders!=null)queryToDQ+="&"+Constants.SRV_PARAM_PROVIDERS+"="+sProviders;
    		if(sMeasurements!=null)queryToDQ+="&"+Constants.SRV_PARAM_MEASUREMENTS+"="+sMeasurements;
    		if(nAltitude > 0)queryToDQ+="&"+Constants.SRV_PARAM_ALTITUDE+"="+nAltitude;
    		if(sOrder!=null)queryToDQ+="&"+Constants.SRV_PARAM_ORDER+"="+sOrder;
    		if ( (lCentroide_lat!=0 || lCentroide_long!=0) && nRadius>0) queryToDQ+="&"+Constants.SRV_PARAM_CENTROID_LATITUDE+"="+lCentroide_lat+"&"+Constants.SRV_PARAM_CENTROID_LONGITUDE+"="+lCentroide_long+"&"+Constants.SRV_PARAM_RADIUS+"="+nRadius;
	
    		log.debug("Method getLatestObservationsBySensor Requested by IP: "+getRemoteAddress(request)+", Request ID: "+ request.getSession().getIdInternal()+ ", Query: "+queryToDQ);
    		jsonDQResponse = GetObservationsFromAFarCloud.getResources(queryToDQ);
    		
    		if(language!=null) jsonAIMResponse = Translator.translateToAIM(jsonDQResponse, language);
    		else jsonAIMResponse = Translator.translateToAIM(jsonDQResponse, "EN");
    		if(jsonAIMResponse.toString().contains("No results found according to the query made"))
    			return Response.status(404).entity("No results Found").build();
//    		return TelemetryExtractor.getOMTelemetryBySensor(request, sMeasurements, hConditions, true);
    		return Response.status(200).entity(jsonAIMResponse.toString()).build();

    }
	
	
}
