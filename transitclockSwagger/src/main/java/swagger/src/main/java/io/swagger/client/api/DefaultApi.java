package io.swagger.client.api;

import io.swagger.client.ApiException;
import io.swagger.client.ApiClient;
import io.swagger.client.Configuration;
import io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import io.swagger.client.model.Attribute;
import io.swagger.client.model.Calendar;
import io.swagger.client.model.Command;
import io.swagger.client.model.CommandType;
import io.swagger.client.model.Device;
import io.swagger.client.model.DeviceTotalDistance;
import io.swagger.client.model.Driver;
import io.swagger.client.model.Event;
import io.swagger.client.model.Geofence;
import io.swagger.client.model.Group;
import io.swagger.client.model.Notification;
import io.swagger.client.model.NotificationType;
import io.swagger.client.model.Permission;
import io.swagger.client.model.Position;
import org.joda.time.DateTime;
import io.swagger.client.model.ReportStops;
import io.swagger.client.model.ReportSummary;
import io.swagger.client.model.ReportTrips;
import io.swagger.client.model.Server;
import io.swagger.client.model.User;
import io.swagger.client.model.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2022-05-02T11:56:05.975-04:00")
public class DefaultApi {
  private ApiClient apiClient;

  public DefaultApi() {
    this(Configuration.getDefaultApiClient());
  }

  public DefaultApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  
  /**
   * Fetch a list of Attributes
   * Without params, it returns a list of Attributes the user has access to
   * @param all Can only be used by admins or managers to fetch all entities (optional)
   * @param userId Standard users can use this only with their own _userId_ (optional)
   * @param deviceId Standard users can use this only with _deviceId_s, they have access to (optional)
   * @param groupId Standard users can use this only with _groupId_s, they have access to (optional)
   * @param refresh  (optional)
   * @return List<Attribute>
   * @throws ApiException if fails to make API call
   */
  public List<Attribute> attributesComputedGet(Boolean all, Integer userId, Integer deviceId, Integer groupId, Boolean refresh) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/attributes/computed".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "all", all));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "refresh", refresh));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Attribute>> localVarReturnType = new GenericType<List<Attribute>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete an Attribute
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void attributesComputedIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling attributesComputedIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/attributes/computed/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update an Attribute
   * 
   * @param id  (required)
   * @param body  (required)
   * @return Attribute
   * @throws ApiException if fails to make API call
   */
  public Attribute attributesComputedIdPut(Integer id, Attribute body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling attributesComputedIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling attributesComputedIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/attributes/computed/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Attribute> localVarReturnType = new GenericType<Attribute>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create an Attribute
   * 
   * @param body  (required)
   * @return Attribute
   * @throws ApiException if fails to make API call
   */
  public Attribute attributesComputedPost(Attribute body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling attributesComputedPost");
    }
    
    // create path and map variables
    String localVarPath = "/attributes/computed".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Attribute> localVarReturnType = new GenericType<Attribute>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Calendars
   * Without params, it returns a list of Calendars the user has access to
   * @param all Can only be used by admins or managers to fetch all entities (optional)
   * @param userId Standard users can use this only with their own _userId_ (optional)
   * @return List<Calendar>
   * @throws ApiException if fails to make API call
   */
  public List<Calendar> calendarsGet(Boolean all, Integer userId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/calendars".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "all", all));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Calendar>> localVarReturnType = new GenericType<List<Calendar>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete a Calendar
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void calendarsIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling calendarsIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/calendars/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update a Calendar
   * 
   * @param id  (required)
   * @param body  (required)
   * @return Calendar
   * @throws ApiException if fails to make API call
   */
  public Calendar calendarsIdPut(Integer id, Calendar body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling calendarsIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling calendarsIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/calendars/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Calendar> localVarReturnType = new GenericType<Calendar>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a Calendar
   * 
   * @param body  (required)
   * @return Calendar
   * @throws ApiException if fails to make API call
   */
  public Calendar calendarsPost(Calendar body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling calendarsPost");
    }
    
    // create path and map variables
    String localVarPath = "/calendars".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Calendar> localVarReturnType = new GenericType<Calendar>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Saved Commands
   * Without params, it returns a list of Drivers the user has access to
   * @param all Can only be used by admins or managers to fetch all entities (optional)
   * @param userId Standard users can use this only with their own _userId_ (optional)
   * @param deviceId Standard users can use this only with _deviceId_s, they have access to (optional)
   * @param groupId Standard users can use this only with _groupId_s, they have access to (optional)
   * @param refresh  (optional)
   * @return List<Command>
   * @throws ApiException if fails to make API call
   */
  public List<Command> commandsGet(Boolean all, Integer userId, Integer deviceId, Integer groupId, Boolean refresh) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/commands".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "all", all));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "refresh", refresh));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Command>> localVarReturnType = new GenericType<List<Command>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete a Saved Command
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void commandsIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling commandsIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/commands/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update a Saved Command
   * 
   * @param id  (required)
   * @param body  (required)
   * @return Command
   * @throws ApiException if fails to make API call
   */
  public Command commandsIdPut(Integer id, Command body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling commandsIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling commandsIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/commands/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Command> localVarReturnType = new GenericType<Command>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a Saved Command
   * 
   * @param body  (required)
   * @return Command
   * @throws ApiException if fails to make API call
   */
  public Command commandsPost(Command body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling commandsPost");
    }
    
    // create path and map variables
    String localVarPath = "/commands".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Command> localVarReturnType = new GenericType<Command>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Saved Commands supported by Device at the moment
   * Return a list of saved commands linked to Device and its groups, filtered by current Device protocol support
   * @param deviceId Standard users can use this only with _deviceId_s, they have access to (optional)
   * @return List<Command>
   * @throws ApiException if fails to make API call
   */
  public List<Command> commandsSendGet(Integer deviceId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/commands/send".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Command>> localVarReturnType = new GenericType<List<Command>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Dispatch commands to device
   * Dispatch a new command or Saved Command if _body.id_ set
   * @param body  (required)
   * @return Command
   * @throws ApiException if fails to make API call
   */
  public Command commandsSendPost(Command body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling commandsSendPost");
    }
    
    // create path and map variables
    String localVarPath = "/commands/send".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Command> localVarReturnType = new GenericType<Command>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of available Commands for the Device or all possible Commands if Device ommited
   * 
   * @param deviceId  (optional)
   * @param textChannel  (optional)
   * @return List<CommandType>
   * @throws ApiException if fails to make API call
   */
  public List<CommandType> commandsTypesGet(Integer deviceId, Boolean textChannel) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/commands/types".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "textChannel", textChannel));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<CommandType>> localVarReturnType = new GenericType<List<CommandType>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Devices
   * Without any params, returns a list of the user&#39;s devices
   * @param all Can only be used by admins or managers to fetch all entities (optional)
   * @param userId Standard users can use this only with their own _userId_ (optional)
   * @param id To fetch one or more devices. Multiple params can be passed like `id=31&amp;id=42` (optional)
   * @param uniqueId To fetch one or more devices. Multiple params can be passed like `uniqueId=333331&amp;uniqieId=44442` (optional)
   * @return List<Device>
   * @throws ApiException if fails to make API call
   */
  public List<Device> devicesGet(Boolean all, Integer userId, Integer id, String uniqueId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/devices".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "all", all));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "id", id));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "uniqueId", uniqueId));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Device>> localVarReturnType = new GenericType<List<Device>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete a Device
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void devicesIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling devicesIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/devices/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update the distance counter of the Device
   * 
   * @param id  (required)
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void devicesIdDistancePut(Integer id, DeviceTotalDistance body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling devicesIdDistancePut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling devicesIdDistancePut");
    }
    
    // create path and map variables
    String localVarPath = "/devices/{id}/distance".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update a Device
   * 
   * @param id  (required)
   * @param body  (required)
   * @return Device
   * @throws ApiException if fails to make API call
   */
  public Device devicesIdPut(Integer id, Device body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling devicesIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling devicesIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/devices/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Device> localVarReturnType = new GenericType<Device>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a Device
   * 
   * @param body  (required)
   * @return Device
   * @throws ApiException if fails to make API call
   */
  public Device devicesPost(Device body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling devicesPost");
    }
    
    // create path and map variables
    String localVarPath = "/devices".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Device> localVarReturnType = new GenericType<Device>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Drivers
   * Without params, it returns a list of Drivers the user has access to
   * @param all Can only be used by admins or managers to fetch all entities (optional)
   * @param userId Standard users can use this only with their own _userId_ (optional)
   * @param deviceId Standard users can use this only with _deviceId_s, they have access to (optional)
   * @param groupId Standard users can use this only with _groupId_s, they have access to (optional)
   * @param refresh  (optional)
   * @return List<Driver>
   * @throws ApiException if fails to make API call
   */
  public List<Driver> driversGet(Boolean all, Integer userId, Integer deviceId, Integer groupId, Boolean refresh) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/drivers".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "all", all));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "refresh", refresh));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Driver>> localVarReturnType = new GenericType<List<Driver>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete a Driver
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void driversIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling driversIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/drivers/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update a Driver
   * 
   * @param id  (required)
   * @param body  (required)
   * @return Driver
   * @throws ApiException if fails to make API call
   */
  public Driver driversIdPut(Integer id, Driver body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling driversIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling driversIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/drivers/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Driver> localVarReturnType = new GenericType<Driver>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a Driver
   * 
   * @param body  (required)
   * @return Driver
   * @throws ApiException if fails to make API call
   */
  public Driver driversPost(Driver body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling driversPost");
    }
    
    // create path and map variables
    String localVarPath = "/drivers".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Driver> localVarReturnType = new GenericType<Driver>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * 
   * 
   * @param id  (required)
   * @return Event
   * @throws ApiException if fails to make API call
   */
  public Event eventsIdGet(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling eventsIdGet");
    }
    
    // create path and map variables
    String localVarPath = "/events/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Event> localVarReturnType = new GenericType<Event>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Geofences
   * Without params, it returns a list of Geofences the user has access to
   * @param all Can only be used by admins or managers to fetch all entities (optional)
   * @param userId Standard users can use this only with their own _userId_ (optional)
   * @param deviceId Standard users can use this only with _deviceId_s, they have access to (optional)
   * @param groupId Standard users can use this only with _groupId_s, they have access to (optional)
   * @param refresh  (optional)
   * @return List<Geofence>
   * @throws ApiException if fails to make API call
   */
  public List<Geofence> geofencesGet(Boolean all, Integer userId, Integer deviceId, Integer groupId, Boolean refresh) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/geofences".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "all", all));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "refresh", refresh));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Geofence>> localVarReturnType = new GenericType<List<Geofence>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete a Geofence
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void geofencesIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling geofencesIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/geofences/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update a Geofence
   * 
   * @param id  (required)
   * @param body  (required)
   * @return Geofence
   * @throws ApiException if fails to make API call
   */
  public Geofence geofencesIdPut(Integer id, Geofence body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling geofencesIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling geofencesIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/geofences/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Geofence> localVarReturnType = new GenericType<Geofence>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a Geofence
   * 
   * @param body  (required)
   * @return Geofence
   * @throws ApiException if fails to make API call
   */
  public Geofence geofencesPost(Geofence body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling geofencesPost");
    }
    
    // create path and map variables
    String localVarPath = "/geofences".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Geofence> localVarReturnType = new GenericType<Geofence>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Groups
   * Without any params, returns a list of the Groups the user belongs to
   * @param all Can only be used by admins or managers to fetch all entities (optional)
   * @param userId Standard users can use this only with their own _userId_ (optional)
   * @return List<Group>
   * @throws ApiException if fails to make API call
   */
  public List<Group> groupsGet(Boolean all, Integer userId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/groups".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "all", all));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Group>> localVarReturnType = new GenericType<List<Group>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete a Group
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void groupsIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling groupsIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update a Group
   * 
   * @param id  (required)
   * @param body  (required)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group groupsIdPut(Integer id, Group body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling groupsIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling groupsIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/groups/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a Group
   * 
   * @param body  (required)
   * @return Group
   * @throws ApiException if fails to make API call
   */
  public Group groupsPost(Group body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling groupsPost");
    }
    
    // create path and map variables
    String localVarPath = "/groups".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Group> localVarReturnType = new GenericType<Group>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Notifications
   * Without params, it returns a list of Notifications the user has access to
   * @param all Can only be used by admins or managers to fetch all entities (optional)
   * @param userId Standard users can use this only with their own _userId_ (optional)
   * @param deviceId Standard users can use this only with _deviceId_s, they have access to (optional)
   * @param groupId Standard users can use this only with _groupId_s, they have access to (optional)
   * @param refresh  (optional)
   * @return List<Notification>
   * @throws ApiException if fails to make API call
   */
  public List<Notification> notificationsGet(Boolean all, Integer userId, Integer deviceId, Integer groupId, Boolean refresh) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/notifications".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "all", all));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "refresh", refresh));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Notification>> localVarReturnType = new GenericType<List<Notification>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete a Notification
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void notificationsIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling notificationsIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/notifications/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update a Notification
   * 
   * @param id  (required)
   * @param body  (required)
   * @return Notification
   * @throws ApiException if fails to make API call
   */
  public Notification notificationsIdPut(Integer id, Notification body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling notificationsIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling notificationsIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/notifications/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Notification> localVarReturnType = new GenericType<Notification>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a Notification
   * 
   * @param body  (required)
   * @return Notification
   * @throws ApiException if fails to make API call
   */
  public Notification notificationsPost(Notification body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling notificationsPost");
    }
    
    // create path and map variables
    String localVarPath = "/notifications".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Notification> localVarReturnType = new GenericType<Notification>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Send test notification to current user via Email and SMS
   * 
   * @throws ApiException if fails to make API call
   */
  public void notificationsTestPost() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/notifications/test".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Fetch a list of available Notification types
   * 
   * @return List<NotificationType>
   * @throws ApiException if fails to make API call
   */
  public List<NotificationType> notificationsTypesGet() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/notifications/types".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<NotificationType>> localVarReturnType = new GenericType<List<NotificationType>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Unlink an Object from another Object
   * 
   * @param body  (required)
   * @throws ApiException if fails to make API call
   */
  public void permissionsDelete(Permission body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling permissionsDelete");
    }
    
    // create path and map variables
    String localVarPath = "/permissions".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Link an Object to another Object
   * 
   * @param body  (required)
   * @return Permission
   * @throws ApiException if fails to make API call
   */
  public Permission permissionsPost(Permission body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling permissionsPost");
    }
    
    // create path and map variables
    String localVarPath = "/permissions".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Permission> localVarReturnType = new GenericType<Permission>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetches a list of Positions
   * Without any params, it returns a list of last known positions for all the user&#39;s Devices. _from_ and _to_ fields are not required with _id_
   * @param deviceId _deviceId_ is optional, but requires the _from_ and _to_ parameters when used (optional)
   * @param from in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (optional)
   * @param to in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (optional)
   * @param id To fetch one or more positions. Multiple params can be passed like `id=31&amp;id=42` (optional)
   * @return List<Position>
   * @throws ApiException if fails to make API call
   */
  public List<Position> positionsGet(Integer deviceId, DateTime from, DateTime to, Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/positions".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "id", id));
    

    

    

    final String[] localVarAccepts = {
      "application/json", "text/csv", "application/gpx+xml"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/csv", "application/gpx+xml"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Position>> localVarReturnType = new GenericType<List<Position>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Events within the time period for the Devices or Groups
   * At least one _deviceId_ or one _groupId_ must be passed
   * @param from in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param to in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param deviceId  (optional)
   * @param groupId  (optional)
   * @param type % can be used to return events of all types (optional)
   * @return List<Event>
   * @throws ApiException if fails to make API call
   */
  public List<Event> reportsEventsGet(DateTime from, DateTime to, List<Integer> deviceId, List<Integer> groupId, List<String> type) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'from' is set
    if (from == null) {
      throw new ApiException(400, "Missing the required parameter 'from' when calling reportsEventsGet");
    }
    
    // verify the required parameter 'to' is set
    if (to == null) {
      throw new ApiException(400, "Missing the required parameter 'to' when calling reportsEventsGet");
    }
    
    // create path and map variables
    String localVarPath = "/reports/events".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "type", type));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    

    

    

    final String[] localVarAccepts = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Event>> localVarReturnType = new GenericType<List<Event>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Positions within the time period for the Devices or Groups
   * At least one _deviceId_ or one _groupId_ must be passed
   * @param from in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param to in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param deviceId  (optional)
   * @param groupId  (optional)
   * @return List<Position>
   * @throws ApiException if fails to make API call
   */
  public List<Position> reportsRouteGet(DateTime from, DateTime to, List<Integer> deviceId, List<Integer> groupId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'from' is set
    if (from == null) {
      throw new ApiException(400, "Missing the required parameter 'from' when calling reportsRouteGet");
    }
    
    // verify the required parameter 'to' is set
    if (to == null) {
      throw new ApiException(400, "Missing the required parameter 'to' when calling reportsRouteGet");
    }
    
    // create path and map variables
    String localVarPath = "/reports/route".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    

    

    

    final String[] localVarAccepts = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Position>> localVarReturnType = new GenericType<List<Position>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of ReportStops within the time period for the Devices or Groups
   * At least one _deviceId_ or one _groupId_ must be passed
   * @param from in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param to in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param deviceId  (optional)
   * @param groupId  (optional)
   * @return List<ReportStops>
   * @throws ApiException if fails to make API call
   */
  public List<ReportStops> reportsStopsGet(DateTime from, DateTime to, List<Integer> deviceId, List<Integer> groupId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'from' is set
    if (from == null) {
      throw new ApiException(400, "Missing the required parameter 'from' when calling reportsStopsGet");
    }
    
    // verify the required parameter 'to' is set
    if (to == null) {
      throw new ApiException(400, "Missing the required parameter 'to' when calling reportsStopsGet");
    }
    
    // create path and map variables
    String localVarPath = "/reports/stops".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    

    

    

    final String[] localVarAccepts = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<ReportStops>> localVarReturnType = new GenericType<List<ReportStops>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of ReportSummary within the time period for the Devices or Groups
   * At least one _deviceId_ or one _groupId_ must be passed
   * @param from in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param to in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param deviceId  (optional)
   * @param groupId  (optional)
   * @return List<ReportSummary>
   * @throws ApiException if fails to make API call
   */
  public List<ReportSummary> reportsSummaryGet(DateTime from, DateTime to, List<Integer> deviceId, List<Integer> groupId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'from' is set
    if (from == null) {
      throw new ApiException(400, "Missing the required parameter 'from' when calling reportsSummaryGet");
    }
    
    // verify the required parameter 'to' is set
    if (to == null) {
      throw new ApiException(400, "Missing the required parameter 'to' when calling reportsSummaryGet");
    }
    
    // create path and map variables
    String localVarPath = "/reports/summary".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    

    

    

    final String[] localVarAccepts = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<ReportSummary>> localVarReturnType = new GenericType<List<ReportSummary>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of ReportTrips within the time period for the Devices or Groups
   * At least one _deviceId_ or one _groupId_ must be passed
   * @param from in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param to in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param deviceId  (optional)
   * @param groupId  (optional)
   * @return List<ReportTrips>
   * @throws ApiException if fails to make API call
   */
  public List<ReportTrips> reportsTripsGet(DateTime from, DateTime to, List<Integer> deviceId, List<Integer> groupId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'from' is set
    if (from == null) {
      throw new ApiException(400, "Missing the required parameter 'from' when calling reportsTripsGet");
    }
    
    // verify the required parameter 'to' is set
    if (to == null) {
      throw new ApiException(400, "Missing the required parameter 'to' when calling reportsTripsGet");
    }
    
    // create path and map variables
    String localVarPath = "/reports/trips".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "deviceId", deviceId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "groupId", groupId));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    

    

    

    final String[] localVarAccepts = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<ReportTrips>> localVarReturnType = new GenericType<List<ReportTrips>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch Server information
   * 
   * @return Server
   * @throws ApiException if fails to make API call
   */
  public Server serverGet() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/server".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Server> localVarReturnType = new GenericType<Server>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Update Server information
   * 
   * @param body  (required)
   * @return Server
   * @throws ApiException if fails to make API call
   */
  public Server serverPut(Server body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling serverPut");
    }
    
    // create path and map variables
    String localVarPath = "/server".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<Server> localVarReturnType = new GenericType<Server>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Close the Session
   * 
   * @throws ApiException if fails to make API call
   */
  public void sessionDelete() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/session".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Fetch Session information
   * 
   * @param token  (optional)
   * @return User
   * @throws ApiException if fails to make API call
   */
  public User sessionGet(String token) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/session".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "token", token));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<User> localVarReturnType = new GenericType<User>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a new Session
   * 
   * @param email  (required)
   * @param password  (required)
   * @return User
   * @throws ApiException if fails to make API call
   */
  public User sessionPost(String email, String password) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'email' is set
    if (email == null) {
      throw new ApiException(400, "Missing the required parameter 'email' when calling sessionPost");
    }
    
    // verify the required parameter 'password' is set
    if (password == null) {
      throw new ApiException(400, "Missing the required parameter 'password' when calling sessionPost");
    }
    
    // create path and map variables
    String localVarPath = "/session".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    if (email != null)
      localVarFormParams.put("email", email);
    if (password != null)
      localVarFormParams.put("password", password);
    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/x-www-form-urlencoded"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<User> localVarReturnType = new GenericType<User>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch server Statistics
   * 
   * @param from in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @param to in IS0 8601 format. eg. `1963-11-22T18:30:00Z` (required)
   * @return List<Statistics>
   * @throws ApiException if fails to make API call
   */
  public List<Statistics> statisticsGet(DateTime from, DateTime to) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'from' is set
    if (from == null) {
      throw new ApiException(400, "Missing the required parameter 'from' when calling statisticsGet");
    }
    
    // verify the required parameter 'to' is set
    if (to == null) {
      throw new ApiException(400, "Missing the required parameter 'to' when calling statisticsGet");
    }
    
    // create path and map variables
    String localVarPath = "/statistics".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "from", from));
    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "to", to));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<Statistics>> localVarReturnType = new GenericType<List<Statistics>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Fetch a list of Users
   * 
   * @param userId Can only be used by admin or manager users (optional)
   * @return List<User>
   * @throws ApiException if fails to make API call
   */
  public List<User> usersGet(String userId) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/users".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<List<User>> localVarReturnType = new GenericType<List<User>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Delete a User
   * 
   * @param id  (required)
   * @throws ApiException if fails to make API call
   */
  public void usersIdDelete(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersIdDelete");
    }
    
    // create path and map variables
    String localVarPath = "/users/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
    
  }
  
  /**
   * Update a User
   * 
   * @param id  (required)
   * @param body  (required)
   * @return User
   * @throws ApiException if fails to make API call
   */
  public User usersIdPut(Integer id, User body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling usersIdPut");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling usersIdPut");
    }
    
    // create path and map variables
    String localVarPath = "/users/{id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<User> localVarReturnType = new GenericType<User>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
  /**
   * Create a User
   * 
   * @param body  (required)
   * @return User
   * @throws ApiException if fails to make API call
   */
  public User usersPost(User body) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling usersPost");
    }
    
    // create path and map variables
    String localVarPath = "/users".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    

    

    

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "basicAuth" };

    
    GenericType<User> localVarReturnType = new GenericType<User>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    
  }
  
}
