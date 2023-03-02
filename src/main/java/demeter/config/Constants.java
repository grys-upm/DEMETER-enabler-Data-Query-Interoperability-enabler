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
package demeter.config;

public class Constants {

	/* services params*/
	public static final String SRV_PARAM_LIMIT = "limit";
	
	public static final String SRV_PARAM_ENTITY_NAMES = "entityNames";	
	public static final String SRV_PARAM_SERVICES = "services";
	public static final String SRV_PARAM_PROVIDERS = "providers";
	public static final String SRV_PARAM_DEVICES = "devices";
	public static final String SRV_PARAM_TYPES = "types";	
	public static final String SRV_PARAM_ALTITUDE = "altitude";
	
	//special => FROM CLAUSE 
	public static final String SRV_PARAM_MEASUREMENTS = "measurements";

	//special => ORDER CLAUSE 
	public static final String SRV_PARAM_ORDER = "order";
	
	// Proximity + georaptor: centroid + radio => 
	public static final String SRV_PARAM_CENTROID_LONGITUDE ="centr_long";
	public static final String SRV_PARAM_CENTROID_LATITUDE ="centr_lat";
	public static final String SRV_PARAM_RADIUS ="radius";
	
	// URL for accesing FCT DEMETER Data
	public static final String DQ_URL = "https://margarita.etsist.upm.es:9153/getObservationsBySensor/latest/?limit=";
//	public static final String DQ_URL = "https://margarita.etsist.upm.es:9104/getObservationsBySensor/latest/?limit=";
	
	//Available Databases
	public static final String fct = "fct";
	
	//Languages
	public static final String EN = "EN";
	public static final String ES = "ES";
	public static final String SRV_PARAM_LANGUAGE ="language";
	
}
