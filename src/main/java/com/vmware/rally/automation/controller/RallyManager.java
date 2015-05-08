package com.vmware.rally.automation.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.CreateRequest;
import com.rallydev.rest.request.DeleteRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.DeleteResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.vmware.rally.automation.data.enums.RTestMethod;
import com.vmware.rally.automation.data.enums.RTestResultVerdict;
import com.vmware.rally.automation.data.enums.RTestType;


/**
 * Manager class for handling Rally API REST calls.
 * Singleton.
 * 
 * @author akaramyan
 */

// TODO: if not initialized throw exception

@SuppressWarnings("unused")
public class RallyManager {
	
	private RallyRestApi _restApi;
	private boolean _initilised;
	private String _apiKey;
	private String _userEmail;
	
	
	private static RallyManager instance = null;
	public static RallyManager getInstance() {
		if(instance == null) {
			instance = new RallyManager();
		}
		return instance;
	}
	
	private RallyManager() { 
		_initilised = false;
	}
	
	
	/* Getters and Setters */
	
	public boolean isInitialized() {
		return _initilised;
	}
	
	public String getApiKey() {
		return _apiKey;
	}
	public void setApiKey(String apiKey) {
		_apiKey = apiKey;
	}
	
	public String getUserEmail() {
		return _userEmail;
	}
	public void setUserEmail(String userEmail) {
		_userEmail = userEmail;
	}
	
	
	/* Public Methods */
	
	public void initialize(String userEmail, String apiKey) {
		_apiKey = apiKey;
		_userEmail = userEmail;
		_restApi = null;
		
		try {
			_restApi = new RallyRestApi(new URI(RALLY_URL_1), _apiKey);
			
			if (_restApi != null) {
				_initilised = true;
			}
		} catch (URISyntaxException e) {
			// NOP
		}
	}

	public JsonObject getUserWithEmail(String email) throws IOException {
		if (!isInitialized()) {
			return null;
		}
		
		QueryRequest userRequest = new QueryRequest(REQUEST_TYPE_USER);
		userRequest.setQueryFilter(new QueryFilter(QUERY_FILTER_EMAIL, QUERY_OPERATOR_EQUALS, email));
		userRequest = setupQueryRequestForBasicSingleResult(userRequest);
		
		QueryResponse queryResponse = _restApi.query(userRequest);
		JsonObject userJson = getFirstResultFromResponse(queryResponse);
		
		return userJson;
	}
	
	public JsonObject getTestCaseWithId(String id) throws IOException {
		if (!isInitialized()) {
			return null;
		}
		
		QueryRequest testCaseRequest = new QueryRequest(REQUEST_TYPE_TESTCASE);
		testCaseRequest.setQueryFilter(new QueryFilter(QUERY_FILTER_ID, QUERY_OPERATOR_EQUALS, id));
		testCaseRequest = setupQueryRequestForBasicSingleResult(testCaseRequest);
		
		QueryResponse queryResponse = _restApi.query(testCaseRequest);
		JsonObject testCaseJson = getFirstResultFromResponse(queryResponse);
		
		return testCaseJson;
	}
	
	public JsonObject getTestSetWithId(String id) throws IOException {
		if (!isInitialized()) {
			return null;
		}
		
		QueryRequest testSetRequest = new QueryRequest(REQUEST_TYPE_TESTSET);
		testSetRequest.setQueryFilter(new QueryFilter(QUERY_FILTER_ID, QUERY_OPERATOR_EQUALS, id));
		testSetRequest = setupQueryRequestForBasicSingleResult(testSetRequest);
		
		QueryResponse queryResponse = _restApi.query(testSetRequest);
		JsonObject testSetJson = getFirstResultFromResponse(queryResponse);

		return testSetJson;
	}
	
	public JsonObject createTestCase(String name, RTestType type, RTestMethod method) throws IOException {
		if (!isInitialized()) {
			return null;
		}
		
		// Default values
		type = RTestType.ACCEPTANCE;
		method = RTestMethod.MANUAL;
		
		JsonObject testCaseJson = new JsonObject();
		testCaseJson.addProperty(JSON_PROPERTY_NAME, name);
		testCaseJson.addProperty(JSON_PROPERTY_TYPE, type.toString());
		testCaseJson.addProperty(JSON_PROPERTY_METHOD, method.toString());
		testCaseJson.add(JSON_PROPERTY_OWNER, getUserWithEmail(_userEmail));
		
    	CreateRequest newTestCaseRequest = new CreateRequest(REQUEST_TYPE_TESTCASE, testCaseJson);
		CreateResponse newTestCaseResponse = _restApi.create(newTestCaseRequest);
		JsonObject createdTestCaseJson = newTestCaseResponse.getObject();
		
		return createdTestCaseJson;
	}
	
	public JsonObject createTestCaseResult(JsonObject testCase, JsonObject testSet, 
			String build, RTestResultVerdict verdict, Date date) throws IOException {
		if (!isInitialized()) {
			return null;
		}
		
		JsonObject testCaseResultJson = new JsonObject();
		testCaseResultJson.add(JSON_PROPERTY_TESTCASE, testCase);		
		testCaseResultJson.add(JSON_PROPERTY_TESTSET, testSet);
		testCaseResultJson.add(JSON_PROPERTY_TESTER, getUserWithEmail(_userEmail));
		testCaseResultJson.addProperty(JSON_PROPERTY_BUILD, build);
		testCaseResultJson.addProperty(JSON_PROPERTY_VERDICT, verdict.toString());
		testCaseResultJson.addProperty(JSON_PROPERTY_DATE, getFormattedDateString(date));

		CreateRequest newTestCaseResultRequest = new CreateRequest(REQUEST_TYPE_TESTCASERESULT, testCaseResultJson);
		CreateResponse newTestCaseResultResponse = _restApi.create(newTestCaseResultRequest);
		JsonObject createdTestCaseResultJson = newTestCaseResultResponse.getObject();

		return createdTestCaseResultJson;
	}
	
	
	/* Helpers */

	/**
	 * Helper method, returns date formatted string with Rally date format
	 * @param date  - Date
	 * @return formatted date string
	 */
	private String getFormattedDateString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat(RALLY_DATE_FORMAT);
		
		return dateFormat.format(date);
	}
	
	/**
	 * Helper method, returns QueryResponse result array first element
	 * @param response - QueryResponse object
	 * @return first element from results if it's a JsonObject; <i>null</i> otherwise
	 */
	private JsonObject getFirstResultFromResponse(QueryResponse response) {
		JsonObject result = null;
		
		if (response.getTotalResultCount() > 0) {
			JsonArray array = response.getResults();
			if (array.get(0).isJsonObject()) {
				result = array.get(0).getAsJsonObject();
			}
		}
		
		return result;
	}

	/**
	 * Helper method, returns QueryRequest setup for retrieving single JsonObject with minimal data 
	 * @param request  - QueryRequest object to setup
	 * @return query request setup for retrieving single JsonObject with minimal data
	 */
	private QueryRequest setupQueryRequestForBasicSingleResult(QueryRequest request) {
		if (request != null) {
			request.setFetch(new Fetch(""));
			request.setLimit(1);
		}
		
		return request;
	}
	
	/**
	 * Helper method, deletes Rally object with given reference
	 * @param ref - String containing reference, ex. "/testcase/7362928857"
	 * @return <i>true</i> if deletion was successful, <i>false</i> otherwise
	 * @throws IOException
	 */
	private boolean deleteObjectWithRef(String ref) throws IOException {
		if (!isInitialized()) {
			return false;
		}
		
		DeleteRequest deleteRequest = new DeleteRequest(ref);
		DeleteResponse deleteResponse = _restApi.delete(deleteRequest);
		
		return deleteResponse.wasSuccessful();
	}
	
	
	/* String Constants */
	
	private static final String RALLY_URL_1 = "https://rally1.rallydev.com";
	private static final String RALLY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	private static final String REQUEST_TYPE_TESTCASE = "testcase";
	private static final String REQUEST_TYPE_TESTCASERESULT = "testcaseresult";
	private static final String REQUEST_TYPE_TESTSET = "testset";
	private static final String REQUEST_TYPE_USER = "user";
	
	private static final String JSON_PROPERTY_BUILD = "Build";
	private static final String JSON_PROPERTY_DATE = "Date";
	private static final String JSON_PROPERTY_METHOD = "Method";
	private static final String JSON_PROPERTY_NAME = "Name";
	private static final String JSON_PROPERTY_OWNER = "Owner";
	private static final String JSON_PROPERTY_TESTCASE = "TestCase";
	private static final String JSON_PROPERTY_TESTER = "Tester";
	private static final String JSON_PROPERTY_TESTSET = "TestSet";
	private static final String JSON_PROPERTY_TYPE = "Type";
	private static final String JSON_PROPERTY_VERDICT = "Verdict";
	
	private static final String QUERY_FILTER_EMAIL = "EmailAddress";
	private static final String QUERY_FILTER_ID = "FormattedID";
	private static final String QUERY_OPERATOR_EQUALS = "=";
	
	/* ****/
	
}
