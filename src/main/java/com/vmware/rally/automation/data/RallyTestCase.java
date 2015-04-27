package com.vmware.rally.automation.data;

public class RallyTestCase {
	
	private String _formattedId = "";
	private String _buildNumber = "";
	private String _testSetId = "";
	
	
	public String getFormattedId() {
		return _formattedId;
	}
	public void setFormattedId(String formattedId) {
		_formattedId = formattedId;
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
}
