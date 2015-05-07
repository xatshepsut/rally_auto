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


/**
 * Manager class for handling Rally API REST calls.
 * Singleton.
 * 
 * @author akaramyan
 */

@SuppressWarnings("unused")
public class RallyManager {
	
	private static RallyRestApi _restApi;
	
	public static String RALLY_URL_1 = "https://rally1.rallydev.com";
	public static String API_KEY = "_OhYE7czYRo2y2tR6il3lyJQsoJGhP0T1gM8JqCFZMlg";
	public static String USER_EMAIL = "bobbrown@dispostable.com";
	
	
	private static final String RALLY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static final String REQUEST_TYPE_TESTCASE = "testcase";
	public static final String REQUEST_TYPE_TESTCASERESULT = "testcaseresult";
	public static final String REQUEST_TYPE_TESTSET = "testset";
	public static final String REQUEST_TYPE_USER = "user";
	
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
	
	
	private static final String TESTCASE_METHOD_VALUE_1 = "Manual";
	private static final String TESTCASE_METHOD_VALUE_2 = "Automated";
	
	private static final String TESTCASE_TYPE_VALUE_1 = "Acceptance";
	private static final String TESTCASE_TYPE_VALUE_2 = "Functional";
	private static final String TESTCASE_TYPE_VALUE_3 = "Performance";
	private static final String TESTCASE_TYPE_VALUE_4 = "Regression";
	private static final String TESTCASE_TYPE_VALUE_5 = "Usability";
	private static final String TESTCASE_TYPE_VALUE_6 = "User Interface";
	
	
	private RallyManager() {
		try {
			_restApi = new RallyRestApi(new URI(RALLY_URL_1), API_KEY);
			
		} catch (URISyntaxException e) {
			//
		}
	}
	
	private static RallyManager instance = null;
	public static RallyManager getInstance() {
		if(instance == null) {
			instance = new RallyManager();
		}
		return instance;
	}
	
	
	/* Public Methods */

	public JsonObject getUserWithEmail(String email) throws IOException {
		if (_restApi == null) {
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
		if (_restApi == null) {
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
		if (_restApi == null) {
			return null;
		}
		
		QueryRequest testSetRequest = new QueryRequest(REQUEST_TYPE_TESTSET);
		testSetRequest.setQueryFilter(new QueryFilter(QUERY_FILTER_ID, QUERY_OPERATOR_EQUALS, id));
		testSetRequest = setupQueryRequestForBasicSingleResult(testSetRequest);
		
		QueryResponse queryResponse = _restApi.query(testSetRequest);
		JsonObject testSetJson = getFirstResultFromResponse(queryResponse);

		return testSetJson;
	}
	
	public JsonObject createTestCase(String name, String type, String method) throws IOException {
		if (_restApi == null) {
			return null;
		}
		
		// Default values
		type = TESTCASE_TYPE_VALUE_1;
		method = TESTCASE_METHOD_VALUE_1;
		
		JsonObject testCaseJson = new JsonObject();
		testCaseJson.addProperty(JSON_PROPERTY_NAME, name);
		testCaseJson.addProperty(JSON_PROPERTY_TYPE, type);
		testCaseJson.addProperty(JSON_PROPERTY_METHOD, method);
		testCaseJson.add(JSON_PROPERTY_OWNER, getUserWithEmail(USER_EMAIL));
		
    	CreateRequest newTestCaseRequest = new CreateRequest(REQUEST_TYPE_TESTCASE, testCaseJson);
		CreateResponse newTestCaseResponse = _restApi.create(newTestCaseRequest);
		JsonObject createdTestCaseJson = newTestCaseResponse.getObject();
		
		return createdTestCaseJson;
	}
	
	public JsonObject createTestCaseResult(JsonObject testCase, JsonObject testSet, String build, String verdict, String date) throws IOException {
		if (_restApi == null) {
			return null;
		}
		
		JsonObject testCaseResultJson = new JsonObject();
		testCaseResultJson.add(JSON_PROPERTY_TESTCASE, testCase);		
		testCaseResultJson.add(JSON_PROPERTY_TESTSET, testSet);
		testCaseResultJson.add(JSON_PROPERTY_TESTER, getUserWithEmail(USER_EMAIL));
		testCaseResultJson.addProperty(JSON_PROPERTY_BUILD, build);
		testCaseResultJson.addProperty(JSON_PROPERTY_VERDICT, verdict);
		testCaseResultJson.addProperty(JSON_PROPERTY_DATE, date);

		CreateRequest newTestCaseResultRequest = new CreateRequest(REQUEST_TYPE_TESTCASERESULT, testCaseResultJson);
		CreateResponse newTestCaseResultResponse = _restApi.create(newTestCaseResultRequest);
		JsonObject createdTestCaseResultJson = newTestCaseResultResponse.getObject();

		return createdTestCaseResultJson;
	}
	
	
	/* Helpers */
	
	/**
	 * Helper method, returns current date string in Rally date format 
	 * @return formatted date string
	 */
	public String getCurrentDate() {
		Date date = new Date();
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
		if (_restApi == null) {
			return false;
		}
		
		DeleteRequest deleteRequest = new DeleteRequest(ref);
		DeleteResponse deleteResponse = _restApi.delete(deleteRequest);
		
		return deleteResponse.wasSuccessful();
	}
}
