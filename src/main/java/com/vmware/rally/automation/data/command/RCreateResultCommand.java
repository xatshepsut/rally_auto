package com.vmware.rally.automation.data.command;

import java.util.Date;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;
import com.vmware.rally.automation.data.enums.RTestResultVerdict;
import com.vmware.rally.automation.exception.InvalidRCommandException;
import com.vmware.rally.automation.exception.RTaskException;
import com.vmware.rally.automation.exception.UninitializedRallyApiException;

/**
 * RCreateResultCommand handles creation of TestCaseResult object in Rally. 
 * Implements RCommand interface.
 * @author akaramyan
 */
public class RCreateResultCommand implements RCommand {
	
	private JsonObject _testCase;
	private JsonObject _testSet;
	private String _build;
	private RTestResultVerdict _verdict;
	private Date _date;
	
	public RCreateResultCommand(JsonObject testCase, JsonObject testSet, String build,
			RTestResultVerdict verdict, Date date) {
		setTestCase(testCase);
		setTestSet(testSet);
		setBuild(build);
		setVerdict(verdict);
		setDate(date);
	}

	public JsonObject execute() throws RTaskException, UninitializedRallyApiException, InvalidRCommandException {
		if (!isValid()) {
			throw new InvalidRCommandException();
		}
		
		JsonObject result = RallyManager.getInstance().createTestCaseResult(_testCase, _testSet, _build, _verdict, _date);
		
        return result;
	}
	
	public boolean isValid() {
		// We assume it's not possible to run TC without referencing TS
		// In case different functionality is also required, it will be add later.
		
		return !(_testCase == null || _testSet == null || (_build == null || _build.isEmpty()) 
				|| _verdict == null || _date == null);
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
	
	public RTestResultVerdict getVerdict() {
		return _verdict;
	}
	public void setVerdict(RTestResultVerdict verdict) {
		_verdict = verdict;
	}
	
	public Date getDate() {
		return _date;
	}
	public void setDate(Date date) {
		_date = date;
	}
}
