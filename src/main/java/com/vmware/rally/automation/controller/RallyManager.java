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
import com.vmware.rally.automation.exception.RTaskException;
import com.vmware.rally.automation.exception.RTaskException.RTaskType;
import com.vmware.rally.automation.exception.UninitializedRallyApiException;


/**
 * Manager class for handling Rally API REST calls.
 * Singleton.
 * 
 * @author akaramyan
 */

@SuppressWarnings("unused")
public class RallyManager {
	
	private RallyRestApi _restApi;
	private boolean _initialized;
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
		_initialized = false;
	}
	
	
	/* Getters and Setters */
	
	public boolean isInitialized() {
		return _initialized;
	}
	
	public String getApiKey() {
		return _apiKey;
	}
	
	public String getUserEmail() {
		return _userEmail;
	}
	
	
	/* Public Methods */
	
	/**
	 * Method for initializing RallyManager with given credentials.
	 * Needs to be called at least once before calling other public methods, 
	 * otherwise UninitializedRallyApiException will be thrown. 
	 * @param userEmail   - String containing user email associated with Rally account
	 * @param apiKey      - String containing Rally API key associated with given email account
	 */
	public void initialize(String userEmail, String apiKey) {
		_apiKey = apiKey;
		_userEmail = userEmail;
		_restApi = null;
		
		try {
			_restApi = new RallyRestApi(new URI(RALLY_URL_1), _apiKey);
			
			if (_restApi != null) {
				_initialized = true;
			}
		} catch (URISyntaxException e) {
			// NOP
		}
	}

	/**
	 * Queries user with given email.
	 * @param email  - String
	 * @return JsonObject
	 * @throws RTaskException
	 * @throws UninitializedRallyApiException
	 */
	public JsonObject getUserWithEmail(String email) throws RTaskException, UninitializedRallyApiException {
		if (!isInitialized()) {
			throw new UninitializedRallyApiException();
		}
		
		QueryRequest userRequest = new QueryRequest(REQUEST_TYPE_USER);
		userRequest.setQueryFilter(new QueryFilter(JSON_PROPERTY_EMAIL, QUERY_OPERATOR_EQUALS, email));
		userRequest = setupQueryRequestForBasicSingleResult(userRequest);
		JsonObject userJson = null;
		
		try {
			QueryResponse queryResponse = _restApi.query(userRequest);
			userJson = getFirstResultFromResponse(queryResponse);
			
			if (!queryResponse.wasSuccessful()) {
				throw new RTaskException(RTaskType.GET_USER, queryResponse.getErrors());
			} else if (userJson == null) {
				String details = "no results found with email " + email;
				throw new RTaskException(RTaskType.GET_USER, details);
			}
		} catch (IOException e) {
			throw new RTaskException();
		}
		
		return userJson;
	}
	
	/**
	 * Queries TestCase with given id.
	 * @param id  - String
	 * @return JsonObject
	 * @throws RTaskException
	 * @throws UninitializedRallyApiException
	 */
	public JsonObject getTestCaseWithId(String id) throws RTaskException, UninitializedRallyApiException {
		if (!isInitialized()) {
			throw new UninitializedRallyApiException();
		}
		
		QueryRequest testCaseRequest = new QueryRequest(REQUEST_TYPE_TESTCASE);
		testCaseRequest.setQueryFilter(new QueryFilter(JSON_PROPERTY_FORMATTEDID, QUERY_OPERATOR_EQUALS, id));
		testCaseRequest = setupQueryRequestForBasicSingleResult(testCaseRequest);
		JsonObject testCaseJson = null;
		
		try {
			QueryResponse queryResponse = _restApi.query(testCaseRequest);
			testCaseJson = getFirstResultFromResponse(queryResponse);
			
			if (!queryResponse.wasSuccessful()) {
				throw new RTaskException(RTaskType.GET_TEST_CASE, queryResponse.getErrors());
			} else if (testCaseJson == null) {
				String details = "no results found with id " + id;
				throw new RTaskException(RTaskType.GET_TEST_CASE, details);
			}
		} catch (IOException e) {
			throw new RTaskException();
		}
		
		return testCaseJson;
	}
	
	/**
	 * Queries TestSet with given id.
	 * @param id  - String
	 * @return JsonObject
	 * @throws RTaskException
	 * @throws UninitializedRallyApiException
	 */
	public JsonObject getTestSetWithId(String id) throws RTaskException, UninitializedRallyApiException {
		if (!isInitialized()) {
			throw new UninitializedRallyApiException();
		}
		
		QueryRequest testSetRequest = new QueryRequest(REQUEST_TYPE_TESTSET);
		testSetRequest.setQueryFilter(new QueryFilter(JSON_PROPERTY_FORMATTEDID, QUERY_OPERATOR_EQUALS, id));
		testSetRequest = setupQueryRequestForBasicSingleResult(testSetRequest);
		JsonObject testSetJson = null;
		
		try {
			QueryResponse queryResponse = _restApi.query(testSetRequest);
			testSetJson = getFirstResultFromResponse(queryResponse);
		
			if (!queryResponse.wasSuccessful()) {
				throw new RTaskException(RTaskType.GET_TEST_SET, queryResponse.getErrors());
			} else if (testSetJson == null) {
				String details = "no results found with id " + id;
				throw new RTaskException(RTaskType.GET_TEST_SET, details);
			}
		} catch (IOException e) {
			throw new RTaskException();
		}
		
		
		return testSetJson;
	}
	
	/**
	 * Creates TestCase with given data and current user as owner.
	 * @param name    - String
	 * @param type    - RTestType
	 * @param method  - RTestMethod
	 * @return JsonObject
	 * @throws RTaskException
	 * @throws UninitializedRallyApiException
	 */
	public JsonObject createTestCase(String name, RTestType type, RTestMethod method) throws RTaskException, UninitializedRallyApiException {
		if (!isInitialized()) {
			throw new UninitializedRallyApiException();
		}
		
		JsonObject testCaseJson = new JsonObject();
		testCaseJson.addProperty(JSON_PROPERTY_NAME, name);
		testCaseJson.addProperty(JSON_PROPERTY_TYPE, type.toString());
		testCaseJson.addProperty(JSON_PROPERTY_METHOD, method.toString());
		testCaseJson.add(JSON_PROPERTY_OWNER, getUserWithEmail(_userEmail));
		
    	CreateRequest newTestCaseRequest = new CreateRequest(REQUEST_TYPE_TESTCASE, testCaseJson);
    	JsonObject createdTestCaseJson = null;

    	try {
			CreateResponse newTestCaseResponse = _restApi.create(newTestCaseRequest);
			
			if (!newTestCaseResponse.wasSuccessful()) {
				throw new RTaskException(RTaskType.CREATE_TEST_CASE, newTestCaseResponse.getErrors());
			}
			
			createdTestCaseJson = newTestCaseResponse.getObject();
			
		} catch (IOException e) {
			throw new RTaskException();
		}
		
		return createdTestCaseJson;
	}
	
	/**
	 * Creates TestCaseResult with given data and current user as tester.
	 * @param testCase   - JsonObject
	 * @param testSet    - JsonObject
	 * @param build      - String
	 * @param verdict    - RTestResultVerdict
	 * @param date       - Date
	 * @return JsonObject
	 * @throws RTaskException
	 * @throws UninitializedRallyApiException
	 */
	public JsonObject createTestCaseResult(JsonObject testCase, JsonObject testSet, String build, 
			RTestResultVerdict verdict, Date date) throws RTaskException, UninitializedRallyApiException {
		if (!isInitialized()) {
			throw new UninitializedRallyApiException();
		}
		
		JsonObject testCaseResultJson = new JsonObject();
		testCaseResultJson.add(JSON_PROPERTY_TESTCASE, testCase);		
		testCaseResultJson.add(JSON_PROPERTY_TESTSET, testSet);
		testCaseResultJson.add(JSON_PROPERTY_TESTER, getUserWithEmail(_userEmail));
		testCaseResultJson.addProperty(JSON_PROPERTY_BUILD, build);
		testCaseResultJson.addProperty(JSON_PROPERTY_VERDICT, verdict.toString());
		testCaseResultJson.addProperty(JSON_PROPERTY_DATE, getFormattedDateString(date));

		CreateRequest newTestCaseResultRequest = new CreateRequest(REQUEST_TYPE_TESTCASERESULT, testCaseResultJson);
		JsonObject createdTestCaseResultJson = null;
		
		try {
			CreateResponse newTestCaseResultResponse = _restApi.create(newTestCaseResultRequest);

			if (!newTestCaseResultResponse.wasSuccessful()) {
				throw new RTaskException(RTaskType.CREATE_TEST_CASE_RESULT, newTestCaseResultResponse.getErrors());
			}

			createdTestCaseResultJson = newTestCaseResultResponse.getObject();
			
		} catch (IOException e) {
			throw new RTaskException();
		}
		
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
	 * @throws RTaskException
	 * @throws UninitializedRallyApiException
	 */
	private boolean deleteObjectWithRef(String ref) throws RTaskException, UninitializedRallyApiException {
		if (!isInitialized()) {
			throw new UninitializedRallyApiException();
		}
		
		DeleteRequest deleteRequest = new DeleteRequest(ref);
		boolean result = false;
		
		try {
			DeleteResponse deleteResponse = _restApi.delete(deleteRequest);
			result = deleteResponse.wasSuccessful();
			
		} catch (IOException e) {
			throw new RTaskException();
		}
		
		return result;
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
	private static final String JSON_PROPERTY_EMAIL = "EmailAddress";
	private static final String JSON_PROPERTY_FORMATTEDID = "FormattedID";
	private static final String JSON_PROPERTY_METHOD = "Method";
	private static final String JSON_PROPERTY_NAME = "Name";
	private static final String JSON_PROPERTY_OWNER = "Owner";
	private static final String JSON_PROPERTY_TESTCASE = "TestCase";
	private static final String JSON_PROPERTY_TESTER = "Tester";
	private static final String JSON_PROPERTY_TESTSET = "TestSet";
	private static final String JSON_PROPERTY_TYPE = "Type";
	private static final String JSON_PROPERTY_VERDICT = "Verdict";
	
	private static final String QUERY_OPERATOR_EQUALS = "=";
	
	/* ****/
	
}
