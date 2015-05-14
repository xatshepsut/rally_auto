package com.vmware.rally.automation.data.command;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;
import com.vmware.rally.automation.data.enums.RJsonObjectType;
import com.vmware.rally.automation.data.enums.RTestMethod;
import com.vmware.rally.automation.data.enums.RTestType;
import com.vmware.rally.automation.exception.InvalidRCommandException;
import com.vmware.rally.automation.exception.RTaskException;
import com.vmware.rally.automation.exception.UninitializedRallyApiException;

/**
 * RCreateCaseCommand handles creation of TestCase object in Rally. 
 * Implements RCommand interface.
 * @author akaramyan
 */
public class RCreateCaseCommand implements RCommand {
	
	private String _name;
	private RTestType _type;
	private RTestMethod _method;

	public RCreateCaseCommand(String name, RTestType type, RTestMethod method) {
		setName(name);
		setType(type);
		setMethod(method);
	}

	public JsonObject execute() throws RTaskException, UninitializedRallyApiException, InvalidRCommandException {
		if (!isValid()) {
			throw new InvalidRCommandException();
		}
		
		JsonObject result = RallyManager.getInstance().createTestCase(_name, _type, _method);

		return result;
	}
	
	public boolean isValid() {
		return !((_name == null || _name.isEmpty()) || _type == null || _method == null);
	}
	
	public RJsonObjectType getResultType() {
		return RJsonObjectType.JSON_OBJECT_TESTCASE;
	}
	
	
	/* Getters and Setters */
	
	public String getName() {
		return _name;
	}
	public void setName(String name) {
		_name = name;
	}
	
	public RTestType getType() {
		return _type;
	}
	public void setType(RTestType type) {
		_type = type;
	}
	
	public RTestMethod getMethod() {
		return _method;
	}
	public void setMethod(RTestMethod method) {
		_method = method;
	}
	
}
