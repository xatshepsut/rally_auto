package com.vmware.rally.automation.data.command;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;

public class RCreateResultCommand implements RCommand {
	private JsonObject _testCase;
	private JsonObject _testSet;
	private String _build;
	private String _verdict;
	
	public RCreateResultCommand(JsonObject testCase, JsonObject testSet, String build, String verdict) {
		_testCase = testCase;
		_testSet = testSet;
		_build = build;
		_verdict = verdict;
	}

	public JsonObject execute() {
		JsonObject result = null;
		
		try {
			result = RallyManager.getInstance().createTestCaseResult(_testCase, _testSet, _build, _verdict);
		} catch (IOException exception) {
			// handle exception, print error etc
		}
		
        return result;
	}
	
}
