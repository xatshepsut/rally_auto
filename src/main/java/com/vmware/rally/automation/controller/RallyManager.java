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

// TODO: move all hardcoded stings
// TODO: add status report logs

@SuppressWarnings("unused")
public class RallyManager {
	
	public static String RALLY_URL_1 = "https://rally1.rallydev.com";
	public static String API_KEY = "_GrAuRENARyBkEqRRLo3XSMcPQUD5Um9vkUBCQjpb0";
	public static String USER_EMAIL = "akaramyan@vmware.com";
	
	public static final String TYPE_TESTCASERESULT = "testcaseresult";
	public static final String TYPE_TESTCASE = "testcase";
	public static final String TYPE_TESTSET = "testset";
	public static final String TYPE_USER = "user";
	
	private static RallyRestApi _restApi;
	
	
	private RallyManager() {
		try {
			// TODO: temporary user credentials are hard coded
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
		QueryRequest userRequest = new QueryRequest(TYPE_USER);
		userRequest.setQueryFilter(new QueryFilter("EmailAddress", "=", email));
		
		// TODO: Common for current filter queries
		userRequest.setFetch(new Fetch(""));
		userRequest.setLimit(1);
		
		QueryResponse queryResponse = _restApi.query(userRequest);
		JsonObject userJson = getFirstResultFromResponse(queryResponse);
		
		return userJson;
	}
	
	public JsonObject getTestCaseWithId(String id) throws IOException {
		QueryRequest testCaseRequest = new QueryRequest(TYPE_TESTCASE);
		testCaseRequest.setQueryFilter(new QueryFilter("FormattedID", "=", id));
		
		// TODO: Common for current filter queries
		testCaseRequest.setFetch(new Fetch(""));
		testCaseRequest.setLimit(1);
		
		QueryResponse queryResponse = _restApi.query(testCaseRequest);
		JsonObject testCaseJson = getFirstResultFromResponse(queryResponse);
		
		return testCaseJson;
	}
	
	public JsonObject getTestSetWithId(String id) throws IOException {
		QueryRequest testCaseRequest = new QueryRequest(TYPE_TESTSET);
		testCaseRequest.setQueryFilter(new QueryFilter("FormattedID", "=", id));
		
		// TODO: Common for current filter queries
		testCaseRequest.setFetch(new Fetch(""));
		testCaseRequest.setLimit(1);
		
		QueryResponse queryResponse = _restApi.query(testCaseRequest);
		JsonObject testSetJson = getFirstResultFromResponse(queryResponse);

		return testSetJson;
	}
	
	// TODO: add properties with defaults
	public JsonObject createTestCase(String name) throws IOException {
		JsonObject testCaseJson = new JsonObject();
		testCaseJson.addProperty("Name", name);
		
		// TODO: user object should be given
		testCaseJson.add("Owner", getUserWithEmail(USER_EMAIL));
		
		
    	CreateRequest newTestCaseRequest = new CreateRequest(TYPE_TESTCASE, testCaseJson);
		CreateResponse newTestCaseResponse = _restApi.create(newTestCaseRequest);
		
		JsonObject createdTestCaseJson = newTestCaseResponse.getObject();
		return createdTestCaseJson;
	}
	
	public JsonObject createTestCaseResult(JsonObject testCase, String build, String verdict) throws IOException {
		JsonObject testCaseResultJson = new JsonObject();
		testCaseResultJson.addProperty("Build", build);
		testCaseResultJson.addProperty("Verdict", verdict);
		
		testCaseResultJson.add("TestCase", testCase);
		
		// TODO: user should provide Date object
		testCaseResultJson.addProperty("Date", getCurrentDate());
		
		// TODO: user object should be given
		testCaseResultJson.add("Tester", getUserWithEmail(USER_EMAIL));
		
		
    	CreateRequest newTestCaseResultRequest = new CreateRequest(TYPE_TESTCASERESULT, testCaseResultJson);
		CreateResponse newTestCaseResultResponse = _restApi.create(newTestCaseResultRequest);
		
		JsonObject createdTestCaseResultJson = newTestCaseResultResponse.getObject();
		return createdTestCaseResultJson;
	}
	
	
	/* Helpers */
	
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
	 * Helper method, returns current date string in Rally date format 
	 * @return formatted date string
	 */
	private String getCurrentDate() {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		
		return dateFormat.format(date);
	}
	
	/**
	 * Helper method, deletes Rally object with given refrence
	 * @param ref - Sring containing refrence, ex. "/testcase/7362928857"
	 * @return <i>true</i> if deletetion was succesfull, <i>false</i> otherwise
	 * @throws IOException
	 */
	private boolean deleteObjectWithRef(String ref) throws IOException {
		DeleteRequest deleteRequest = new DeleteRequest(ref);
		DeleteResponse deleteResponse = _restApi.delete(deleteRequest);
		
		return deleteResponse.wasSuccessful();
	}
}
