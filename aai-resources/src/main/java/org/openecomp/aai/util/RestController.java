/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.logging.LoggingContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;

public class RestController {

	private static EELFLogger LOGGER = EELFManager.getInstance().getLogger(RestController.class);
	
	private static Client client = null;
	
	private String restSrvrBaseURL;
	
	//To do - Come up with helper function that will automatically
	//generate the REST API path based on path parameter(s) and query parameter(s)!
	public static final String REST_APIPATH_COMPLEXES = "cloud-infrastructure/complexes";
	public static final String REST_APIPATH_COMPLEX = "cloud-infrastructure/complexes/complex/";
	public static final String REST_APIPATH_PSERVERS = "cloud-infrastructure/pservers";
	public static final String REST_APIPATH_PSERVER = "cloud-infrastructure/pservers/pserver/";
	public static final String REST_APIPATH_PHYSICALLINKS = "network/physical-links/";
	public static final String REST_APIPATH_PHYSICALLINK = "network/physical-links/physical-link/";
	public static final String REST_APIPATH_PINTERFACES = "network/p-interfaces/";
	public static final String REST_APIPATH_PINTERFACE = "network/p-interfaces/p-interface/";
	public static final String REST_APIPATH_VPLSPES = "network/vpls-pes/";
	public static final String REST_APIPATH_VPLSPE = "network/vpls-pes/vpls-pe/";
	public static final String REST_APIPATH_UPDATE = "actions/update/";
	public static final String REST_APIPATH_SEARCH = "search/nodes-query?search-node-type=";
	
	public static final String REST_APIPATH_CLOUDREGION = "cloud-infrastructure/cloud-regions/cloud-region/";
	public static final  String REST_APIPATH_TENANT = "cloud-infrastructure/tenants/tenant/";
	public static final  String REST_APIPATH_VPE = "network/vpes/vpe/";
	public static final String REST_APIPATH_VIRTUAL_DATA_CENTER = "cloud-infrastructure/virtual-data-centers/virtual-data-center/";
	public static final String REST_APIPATH_VIRTUAL_DATA_CENTERS = "cloud-infrastructure/virtual-data-centers/";
	//network/generic-vnfs/generic-vnf/{vnf-id}
	public static final String REST_APIPATH_GENERIC_VNF = "network/generic-vnfs/generic-vnf/";
	public static final String REST_APIPATH_GENERIC_VNFS = "network/generic-vnfs";
	public static final String REST_APIPATH_L3_NETWORK = "network/l3-networks/l3-network/";
	public static final String REST_APIPATH_L3_NETWORKS = "network/l3-networks";
	public static final String REST_APIPATH_INSTANCE_GROUP = "network/instance-groups/instance-group";
	public static final String REST_APIPATH_INSTANCE_GROUPS = "network/instance-groups";
	
	public static final  String REST_APIPATH_VCE = "network/vces/vce/";
	
	public static final  String REST_APIPATH_SERVICE = "service-design-and-creation/services/service/";
	public static final String REST_APIPATH_LOGICALLINKS = "network/logical-links/";
	public static final String REST_APIPATH_LOGICALLINK = "network/logical-links/logical-link/";
	
	/**
	 * Inits the rest client.
	 *
	 * @throws AAIException the AAI exception
	 */
	private static void initRestClient() throws AAIException
	{
		if (client == null) {
			try {
				String useBasicAuth = AAIConfig.get("aai.tools.enableBasicAuth");
                                if (useBasicAuth != null && useBasicAuth.equals("true")) {

                                        client = HttpsAuthClient.getBasicAuthClient();
                                } else {
                                        client = HttpsAuthClient.getTwoWaySSLClient();
                                }

			}
			catch (KeyManagementException e){
				throw new AAIException("AAI_7117", "KeyManagementException in REST call to DB: " + e.toString());
			} catch (Exception e) {
				throw new AAIException("AAI_7117", " Exception in REST call to DB: " + e.toString());
			}
		}
	}
	
	/**
	 * Sets the rest srvr base URL.
	 *
	 * @param baseURL the base URL
	 * @throws AAIException the AAI exception
	 */
	public void SetRestSrvrBaseURL(String baseURL) throws AAIException
	{
		if (baseURL == null)
			throw new AAIException("AAI_7117", "REST Server base URL cannot be null.");
		restSrvrBaseURL = baseURL;
	}
	
	/**
	 * Gets the rest srvr base URL.
	 *
	 * @return the rest srvr base URL
	 */
	public String getRestSrvrBaseURL() 
	{
		return restSrvrBaseURL;
	}
	
	/**
	 * To do - optimization and automation.  Also make it as generic as possible.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param sourceID the source ID
	 * @param transId the trans id
	 * @param path the path
	 * @param restObject the rest object
	 * @param oldserver the oldserver
	 * @throws AAIException the AAI exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> void Get(T t, String sourceID,  String transId,  String path, RestObject<T> restObject, boolean oldserver) throws AAIException {
		String methodName = "Get";
		String url="";
		transId += ":" + UUID.randomUUID().toString();

		LoggingContext.save();
		LoggingContext.partnerName(sourceID);

		LOGGER.debug(methodName + " start");
	
		restObject.set(t);
		
		if (oldserver)
			url = AAIConfig.get(AAIConstants.AAI_OLDSERVER_URL) + path;
		else
			url = AAIConfig.get(AAIConstants.AAI_SERVER_URL) + path;
		initRestClient();
		LOGGER.debug(url + " for the get REST API");
		ClientResponse cres = client.resource(url)
	         .accept("application/json")
	         .header("X-TransactionId", transId)
	         .header("X-FromAppId",  sourceID)
		 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
	         .type("application/json")
	         .get(ClientResponse.class);

//			System.out.println("cres.EntityInputSream()="+cres.getEntityInputStream().toString());
//			System.out.println("cres.tostring()="+cres.toString());
			
		 if (cres.getStatus() == 200) {
//		     System.out.println(methodName + ": url=" + url);
			 t = (T) cres.getEntity(t.getClass());
			 restObject.set(t);
			 LOGGER.debug(methodName + "REST api GET was successfull!");		    
		 } else {
		 	 LoggingContext.restore();
//		     System.out.println(methodName + ": url=" + url + " failed with status=" + cres.getStatus());
		     throw new AAIException("AAI_7116", methodName +" with status="+cres.getStatus()+", url="+url);
		 }

		 LoggingContext.restore();
	}
	
	
	/**
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param sourceID the source ID
	 * @param transId the trans id
	 * @param path the path
	 * @param restObject the rest object
	 * @param apiVersion the api version
	 * @throws AAIException the AAI exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> void Get(T t, String sourceID,  String transId,  String path, RestObject<T> restObject, String apiVersion) throws AAIException {
		String methodName = "Get";
		String url="";
		transId += ":" + UUID.randomUUID().toString();
		LOGGER.debug(methodName + " start");
	
		restObject.set(t);
		
		url = AAIConfig.get(AAIConstants.AAI_SERVER_URL_BASE) + apiVersion + "/"+ path;
		
		initRestClient();
		LOGGER.debug(url + " for the get REST API");
		ClientResponse cres = client.resource(url)
	         .accept("application/json")
	         .header("X-TransactionId", transId)
	         .header("X-FromAppId",  sourceID)
		 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
	         .type("application/json")
	         .get(ClientResponse.class);

//			System.out.println("cres.EntityInputSream()="+cres.getEntityInputStream().toString());
//			System.out.println("cres.tostring()="+cres.toString());
			
		 if (cres.getStatus() == 200) {
//		     System.out.println(methodName + ": url=" + url);
			 t = (T) cres.getEntity(t.getClass());
			 restObject.set(t);
			 LOGGER.debug(methodName + "REST api GET was successfull!");
		    
		 } else {
//		     System.out.println(methodName + ": url=" + url + " failed with status=" + cres.getStatus());
		     throw new AAIException("AAI_7116", methodName +" with status="+cres.getStatus()+", url="+url);
		 }

	}
	
	/**
	 * Gets the.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param requestObj the request obj
	 * @throws AAIException the AAI exception
	 */
	public static <T> void Get(T t, Request<T> requestObj) throws AAIException {
		String methodName = "Get";
		String url="";
		String transId = requestObj.transactionId;
		transId += ":" + UUID.randomUUID().toString();
		LOGGER.debug(methodName + " start");
	
		requestObj.restObj.set(t);
		
		if (requestObj.oldServer)
			url = AAIConfig.get(AAIConstants.AAI_SERVER_URL_BASE) + "server/" + requestObj.path;
		else
			url = AAIConfig.get(AAIConstants.AAI_SERVER_URL_BASE) + requestObj.path;
		initRestClient();
		
	    try {
		    URL urlObj= new URL(url);
			URI uri = new URI(urlObj.getProtocol(), urlObj.getUserInfo(), urlObj.getHost(), urlObj.getPort(), urlObj.getPath(), urlObj.getQuery(), urlObj.getRef());
			url = uri.toASCIIString();
	    } catch (URISyntaxException | MalformedURLException e) {
			 throw new AAIException("AAI_7116", "bad URL");

		}
		LOGGER.debug(url + " for the get REST API");
		ClientResponse cres = client.resource(url)
	         .accept("application/json")
	         .header("X-TransactionId", transId)
	         .header("X-FromAppId",  requestObj.fromAppId)
		 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
	         .type("application/json")
	         .get(ClientResponse.class);

//			System.out.println("cres.EntityInputSream()="+cres.getEntityInputStream().toString());
//			System.out.println("cres.tostring()="+cres.toString());
			
		 if (cres.getStatus() == 200) {
//		     System.out.println(methodName + ": url=" + url);
			 t = (T) cres.getEntity(t.getClass());
			 requestObj.restObj.set(t);
			 LOGGER.debug(methodName + "REST api GET was successfull!");
		    
		 } else {
//		     System.out.println(methodName + ": url=" + url + " failed with status=" + cres.getStatus());
		     throw new AAIException("AAI_7116", methodName +" with status="+cres.getStatus()+", url="+url);
		 }

	}
	
	/**
	 * Put.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param requestObj the request obj
	 * @throws AAIException the AAI exception
	 */
	public static <T> void Put(T t, Request<T> requestObj) throws AAIException {
		String methodName = "Put";
		String url="";
		String transId = requestObj.transactionId;
		transId += ":" + UUID.randomUUID().toString();
		LOGGER.debug(methodName + " start");		

		initRestClient();
		
		if (requestObj.oldServer)
			url = AAIConfig.get(AAIConstants.AAI_SERVER_URL_BASE) + "server/" + requestObj.path;
		else
			url = AAIConfig.get(AAIConstants.AAI_SERVER_URL_BASE) + requestObj.path;
		
	    try {
		    URL urlObj= new URL(url);
			URI uri = new URI(urlObj.getProtocol(), urlObj.getUserInfo(), urlObj.getHost(), urlObj.getPort(), urlObj.getPath(), urlObj.getQuery(), urlObj.getRef());
			url = uri.toASCIIString();
	    } catch (URISyntaxException | MalformedURLException e) {
			 throw new AAIException("AAI_7116", "bad URL");

		}
		ClientResponse cres = client.resource(url)
	         .accept("application/json")
	         .header("X-TransactionId", transId)
	         .header("X-FromAppId",  requestObj.fromAppId)
		 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
	         .type("application/json")
	         .entity(t)
	         .put(ClientResponse.class);
			
		int statuscode = cres.getStatus();
		if ( statuscode >= 200 && statuscode <= 299 ) {
			 LOGGER.debug(methodName+": url=" + url + ", request=" + url);
		 } else {
			 throw new AAIException("AAI_7116", methodName +" with status="+statuscode+", url="+url);
		 }			 
	}
	
	/**
	 *  Multiple Generic Get.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param sourceID the source ID
	 * @param transId the trans id
	 * @param path the path
	 * @param oldserver the oldserver
	 * @return the list
	 * @throws AAIException the AAI exception
	 */
	public static <T> List<T> Get(T t, String sourceID,  String transId,  String path, boolean oldserver) throws AAIException {
		String methodName = "Get";
		String url="";
		transId += ":" + UUID.randomUUID().toString();
		LOGGER.debug(methodName + " start");
	
		List<T> list;
		
		try {
			if (oldserver)
				url = AAIConfig.get(AAIConstants.AAI_OLDSERVER_URL) + path;
			else
				url = AAIConfig.get(AAIConstants.AAI_SERVER_URL) + path;
			initRestClient();
			LOGGER.debug(url + " for the get REST API");
			ClientResponse cres = client.resource(url)
		         .accept("application/json")
		         .header("X-TransactionId", transId)
		         .header("X-FromAppId",  sourceID)
			 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
		         .type("application/json")
		         .get(ClientResponse.class);


			//System.out.println("cres.EntityInputSream()="+cres.getEntityInputStream().toString());
			//System.out.println("cres.tostring()="+cres.toString());
			
			 if (cres.getStatus() == 200) {
			     //System.out.println(methodName + ": url=" + url);
			     //list = cres.getEntity(new GenericType<List<T>>() { });
			     String datainJson = cres.getEntity(String.class);
			     list = mapJsonToObjectList(t, datainJson, t.getClass());
	
				 LOGGER.debug(methodName + "REST api GET was successfull!");
			     return list;
				 
			 } else {
			     throw new AAIException("AAI_7116", methodName +" with status="+cres.getStatus()+", url="+url);
			 }
		} catch (AAIException e) {
			throw new AAIException("AAI_7116", methodName + " with url="+url+ ", Exception: " + e.toString());
		} catch (Exception e)
		{
			throw new AAIException("AAI_7116", methodName + " with url="+url+ ", Exception: " + e.toString());
		
		}

	}

   /**
    * Map json to object list.
    *
    * @param <T> the generic type
    * @param typeDef the type def
    * @param json the json
    * @param clazz the clazz
    * @return the list
    * @throws Exception the exception
    */
   private static <T> List<T> mapJsonToObjectList(T typeDef,String json, Class clazz) throws Exception
   {
      List<T> list;
      ObjectMapper mapper = new ObjectMapper();
      System.out.println(json);
      TypeFactory t = TypeFactory.defaultInstance();
      list = mapper.readValue(json, t.constructCollectionType(ArrayList.class,clazz));

      return list;
   }
	   
	/**
	 * Put.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param sourceID the source ID
	 * @param transId the trans id
	 * @param path the path
	 * @throws AAIException the AAI exception
	 */
	public static <T> void Put(T t, String sourceID,  String transId,  String path) throws AAIException {
		Put( t, sourceID, transId, path, false);
	}

	/**
	 * Put.
	 *
	 * @param <T> the generic type
	 * @param t the t
	 * @param sourceID the source ID
	 * @param transId the trans id
	 * @param path the path
	 * @param oldserver the oldserver
	 * @throws AAIException the AAI exception
	 */
	public static <T> void Put(T t, String sourceID,  String transId,  String path, boolean oldserver) throws AAIException {
		String methodName = "Put";
		String url="";
		transId += ":" + UUID.randomUUID().toString();

		LOGGER.debug(methodName + " start");		

		initRestClient();
		
		if (oldserver)
			url = AAIConfig.get(AAIConstants.AAI_OLDSERVER_URL) + path;
		else
			url = AAIConfig.get(AAIConstants.AAI_SERVER_URL) + path;
		
		ClientResponse cres = client.resource(url)
	         .accept("application/json")
	         .header("X-TransactionId", transId)
	         .header("X-FromAppId",  sourceID)
		 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
	         .type("application/json")
	         .entity(t)
	         .put(ClientResponse.class);
	
//			System.out.println("cres.tostring()="+cres.toString());
		
		int statuscode = cres.getStatus();
		if ( statuscode >= 200 && statuscode <= 299 ) {
			 LOGGER.debug(methodName+": url=" + url + ", request=" + path);
		 } else {
			 throw new AAIException("AAI_7116", methodName +" with status="+statuscode+", url="+url + ", msg=" + cres.getEntity(String.class));
		 }			 
	}
	
	/**
	 * Delete.
	 *
	 * @param requestObj the request obj
	 * @throws AAIException the AAI exception
	 */
	public static void Delete(Request requestObj) throws AAIException {
		String methodName = "Delete";

		String url="";
		String transId = requestObj.transactionId;
		transId += ":" + UUID.randomUUID().toString();

		LOGGER.debug(methodName + " start");		

		initRestClient();
		
		if (requestObj.oldServer)
			url = AAIConfig.get(AAIConstants.AAI_SERVER_URL_BASE) + "servers/" + requestObj.path;
		else
			url = AAIConfig.get(AAIConstants.AAI_SERVER_URL_BASE) + requestObj.path;
		
	    try {
		    URL urlObj= new URL(url);
			URI uri = new URI(urlObj.getProtocol(), urlObj.getUserInfo(), urlObj.getHost(), urlObj.getPort(), urlObj.getPath(), urlObj.getQuery(), urlObj.getRef());
			url = uri.toASCIIString();
	    } catch (URISyntaxException | MalformedURLException e) {
			 throw new AAIException("AAI_7116", "bad URL");

		}
	
		ClientResponse cres = client.resource(url)
		         .accept("application/json")
		         .header("X-TransactionId", transId)
		         .header("X-FromAppId",  requestObj.fromAppId)
			 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
		         .type("application/json")
		         .entity("{}")
		         .delete(ClientResponse.class);
			
		 if (cres.getStatus() == 204) {
			 LOGGER.debug(methodName+": url=" + url);
		 } else {
			 throw new AAIException("AAI_7116", methodName +" with status="+cres.getStatus()+", url="+url);
		 } 

	}
	
	/**
	 * Delete.
	 *
	 * @param sourceID the source ID
	 * @param transId the trans id
	 * @param path the path
	 * @throws AAIException the AAI exception
	 */
	public static void Delete(String sourceID,  String transId,  String path) throws AAIException {
		String methodName = "Delete";
		String url="";
		transId += ":" + UUID.randomUUID().toString();
		LOGGER.debug(methodName + " start");
		
		initRestClient();
		String request = "{}";
		url = AAIConfig.get(AAIConstants.AAI_SERVER_URL) + path;			
		ClientResponse cres = client.resource(url)
		         .accept("application/json")
		         .header("X-TransactionId", transId)
		         .header("X-FromAppId",  sourceID)
			 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
		         .type("application/json")
		         .entity(request)
		         .delete(ClientResponse.class);
			
		if (cres.getStatus() == 404) { // resource not found
			LOGGER.info("Resource does not exist...: " + cres.getStatus()
					+ ":" + cres.getEntity(String.class));
		} else if (cres.getStatus() == 200  || cres.getStatus() == 204){
			LOGGER.info("Resource " + url + " deleted");
		} else {
			LOGGER.error("Deleting Resource failed: " + cres.getStatus()
				+ ":" + cres.getEntity(String.class));
            throw new AAIException("AAI_7116", "Error during DELETE");
		}
	}
	
    /**
     * Post.
     *
     * @param <T> the generic type
     * @param t the t
     * @param sourceID the source ID
     * @param transId the trans id
     * @param path the path
     * @return the string
     * @throws Exception the exception
     */
    public static <T> String Post(T t, String sourceID,  String transId,  String path) throws Exception {
        String methodName = "Post";
        String url="";
        transId += ":" + UUID.randomUUID().toString();

        LOGGER.debug(methodName + " start");        
        
        try {
            
            initRestClient();    
    
            url = AAIConfig.get(AAIConstants.AAI_SERVER_URL) + path;
            
            ClientResponse cres = client.resource(url)
                 .accept("application/json")
                 .header("X-TransactionId", transId)
                 .header("X-FromAppId",  sourceID)
		 .header("Authorization", HttpsAuthClient.getBasicAuthHeaderValue())
                 .type("application/json")
                 .entity(t)
                 .post(ClientResponse.class);
            
            int statuscode = cres.getStatus();
    		if ( statuscode >= 200 && statuscode <= 299 ) {    
                 LOGGER.debug(methodName + "REST api POST was successful!");
                 return cres.getEntity(String.class);
             } else {
                 throw new AAIException("AAI_7116", methodName +" with status="+statuscode+", url="+url + ", msg=" + cres.getEntity(String.class));
             }    
        
        } catch (AAIException e) {
            throw new AAIException("AAI_7116", methodName + " with url="+url+ ", Exception: " + e.toString());
        } catch (Exception e)
        {
            throw new AAIException("AAI_7116", methodName + " with url="+url+ ", Exception: " + e.toString());
        
        }
    }

	
    /**
     * Gets the single instance of RestController.
     *
     * @param <T> the generic type
     * @param clazz the clazz
     * @return single instance of RestController
     * @throws IllegalAccessException the illegal access exception
     * @throws InstantiationException the instantiation exception
     */
    public static <T> T getInstance(Class<T> clazz) throws IllegalAccessException, InstantiationException
	{
		return clazz.newInstance();
	} 
	
    /**
     * Does resource exist.
     *
     * @param <T> the generic type
     * @param resourcePath the resource path
     * @param resourceClassName the resource class name
     * @param fromAppId the from app id
     * @param transId the trans id
     * @return the t
     */
    /*
     *     DoesResourceExist
     *     
     *     To check whether a resource exist or get a copy of the existing version of the resource
     *	
     *       Resourcepath: should contain the qualified resource path (including encoded unique key identifier value),
     *       resourceClassName: is the canonical name of the resource class name, 
     *       fromAppId:
     *       transId:
     *       
     *     Will return null (if the resource doesn’t exist)  (or) 
     *     Will return the specified resource from the Graph.
     *     
     *     Example:
     *     LogicalLink llink = new LogicalLink();
     *     String resourceClassName = llink.getClass().getCanonicalName();
     *     llink = RestController.DoesResourceExist("network/logical-links/logical-link/" + <encoded-link-name>, resourceClassName, fromAppId, transId);
   */
	public static <T> T DoesResourceExist(String resourcePath, String resourceClassName, String fromAppId, String transId) {
					
		try {
			
			RestObject<T> restObj = new RestObject<T>();
			@SuppressWarnings("unchecked")
			T resourceObj = (T)getInstance(Class.forName(resourceClassName));
			restObj.set(resourceObj);
			RestController.<T>Get(resourceObj, fromAppId, transId, resourcePath, restObj, false);
			
			resourceObj = restObj.get();
			if (resourceObj != null)
			  return resourceObj;

		} catch (AAIException e) {
			
		} catch (ClientHandlerException che) {
			
		}catch (Exception e) {
			
		}
		
		return null;
	}
	
}