package com.vmware.rally.automation.data.command;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;

/**
 * RCreateResultCommand handles creation of TestCaseResult object in Rally. 
 * Implements RCommand interface.
 * @author akaramyan
 */
public class RCreateResultCommand implements RCommand {
	
	private JsonObject _testCase = null;
	private JsonObject _testSet = null;
	private String _build = "";
	private String _verdict = "";
	private String _date = "";
	
	public RCreateResultCommand(JsonObject testCase, JsonObject testSet, String build, String verdict, String date) {
		setTestCase(testCase);
		setTestSet(testSet);
		setBuild(build);
		setVerdict(verdict);
		setDate(date);
	}

	public JsonObject execute() {
		if (!isValid()) {
			// TODO: throw exception
			return null;
		}
		
		JsonObject result = null;
		
		try {
			result = RallyManager.getInstance().createTestCaseResult(_testCase, _testSet, _build, _verdict, _date);
		} catch (IOException exception) {
			// TODO: handle
		}
		
        return result;
	}
	
	public boolean isValid() {
		// We assume it's not possible to run TC without referencing TS
		// In case different functionality is also required, it will be add later.
		
		return !(_testCase == null || _testSet == null || 
				_build.isEmpty() || _verdict.isEmpty() || _date.isEmpty());
	}
	
	
	/* Getters and Setters */
	
	public JsonObject getTestCase() {
		return _testCase;
	}
	public void setTestCase(JsonObject testCase) {
		_testCase = testCase;
	}

	public JsonObject getTestSet() {
		return _testSet;
	}
	public void setTestSet(JsonObject testSet) {
		_testSet = testSet;
	}

	public String getBuild() {
		return _build;
	}
	public void setBuild(String build) {
		_build = build;
	}
	
	public String getVerdict() {
		return _verdict;
	}
	public void setVerdict(String verdict) {
		_verdict = verdict;
	}
	
	public String getDate() {
		return _date;
	}
	public void setDate(String date) {
		_date = date;
	}
}
