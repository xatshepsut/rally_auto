package com.vmware.rally.automation.data;

import com.google.gson.JsonObject;

/**
 * Data container for keeping info retrieved from annotations
 * and test case Json object in it's different states
 * 
 * @author akaramyan
 */
public class RallyTestCase {
	
	private String _id = "";
	private String _buildNumber = "";
	private String _testSetId = "";
	
	private JsonObject _testCaseJson = null;
	
	
	/* Constructors */
	
	public RallyTestCase(String id) {
		setId(id);
	}
	
	public RallyTestCase(String id, String buildNumber) {
		this(id);
		setBuildNumber(buildNumber);
	}
	
	public RallyTestCase(String id, String buildNumber, String testSetId) {
		this(id, buildNumber);
		setTestSetId(testSetId);
	}
	
	/* Public methods */
	
	
	
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
	public boolean hasTestSet() {
		return !_testSetId.isEmpty();
	}
	
	public JsonObject getTestCaseJson() {
		return _testCaseJson;
	}
	public void setestCaseJson(JsonObject testCaseJson) {
		_testCaseJson = testCaseJson;
	}
}
