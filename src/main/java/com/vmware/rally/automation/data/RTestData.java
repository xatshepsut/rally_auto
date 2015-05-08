package com.vmware.rally.automation.data;

import com.google.gson.JsonObject;

/**
 * Data container for keeping data about test:
 * data retrieved from annotations and JSON objects
 * 
 * @author akaramyan
 */
public class RTestData {
	
	private String _id = "";
	private String _buildNumber = "";
	private String _testSetId = "";
	
	private JsonObject _testCaseJson = null;
	private JsonObject _testSetJson = null;
	
	
	/* Constructors */
	
	public RTestData(String id) {
		setId(id);
	}
	
	public RTestData(String id, String buildNumber) {
		this(id);
		setBuildNumber(buildNumber);
	}
	
	public RTestData(String id, String buildNumber, String testSetId) {
		this(id, buildNumber);
		setTestSetId(testSetId);
	}
	
	
	/* Getters and Setters */
	
	public String getId() {
		return _id;
	}
	public void setId(String id) {
		_id = id;
	}
	
	public String getBuildNumber() {
		return _buildNumber;
	}
	public void setBuildNumber(String buildNumber) {
		_buildNumber = buildNumber;
	}
	
	public String getTestSetId() {
		return _testSetId;
	}
	public void setTestSetId(String testSetId) {
		_testSetId = testSetId;
	}
	
	public JsonObject getTestCaseJson() {
		return _testCaseJson;
	}
	public void setTestCaseJson(JsonObject testCaseJson) {
		_testCaseJson = testCaseJson;
	}
	public boolean hasTestCase() {
		return _testCaseJson != null;
	}

	public JsonObject getTestSetJson() {
		return _testSetJson;
	}
	public void setTestSetJson(JsonObject testSetJson) {
		_testSetJson = testSetJson;
	}
	public boolean hasTestSet() {
		return _testSetJson != null;
	}
	
}
