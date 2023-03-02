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

import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import io.swagger.jaxrs.config.BeanConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Api class.
 *
 */
public class Api {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "https://0.0.0.0:9150/";
    public static AtomicInteger counter = new AtomicInteger();
    
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer getServerLookup() {

    	String resources = "demeter.rest";

    	BeanConfig beanConfig = new BeanConfig();

    	beanConfig.setVersion("1.0.1");

    	beanConfig.setSchemes(new String[] { "http" });

    	beanConfig.setBasePath("");

    	beanConfig.setResourcePackage(resources);

    	beanConfig.setScan(true);

    	final ResourceConfig resourceConfig = new ResourceConfig().packages("demeter.demeterTranslator");

    	resourceConfig.packages(resources);

    	resourceConfig.register(io.swagger.jaxrs.listing.ApiListingResource.class);

    	resourceConfig.register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    	resourceConfig.register(JacksonFeature.class);

    	resourceConfig.register(JacksonJsonProvider.class);
    	
		SSLContextConfigurator sslConfig = new SSLContextConfigurator();
		sslConfig.setKeyStoreFile("src/SSL/margarita_java.jks");
		sslConfig.setKeyPass("m4rg4rit4.etsist.upm.es");
    	
//        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), resourceConfig);
    	return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), resourceConfig, true, new SSLEngineConfigurator(sslConfig).setClientMode(false).setNeedClientAuth(false),false);

    }

    /**
     * Api method.
     * @param args
     * @throws IOException
     * @throws ProcessingException 
     */
    public static void main(String[] args) throws IOException, ProcessingException {
    	final HttpServer server = getServerLookup();
        server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true); 

        server.start();
        ClassLoader loader = Api.class.getClassLoader();
        CLStaticHttpHandler docsHandler = new CLStaticHttpHandler(loader, "swagger-ui/");
        
        docsHandler.setFileCacheEnabled(false);

        ServerConfiguration cfg = server.getServerConfiguration();

        cfg.addHttpHandler(docsHandler, "/docs/");
    	
//        Method to load the ids
        Loader.loadIds(System.getProperty("user.dir")+File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"devIds.conf");
//        Method to load the schema
        Loader.loadSchema();
//        Method to load the example response
        Loader.loadJSONResponse();
        
        
        String log4jConfPath = System.getProperty("user.dir")+File.separator+"src"+File.separator+"properties"+File.separator+"log4j.properties";
        
        PropertyConfigurator.configure(log4jConfPath);

        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.shutdownNow();  
    }
}

