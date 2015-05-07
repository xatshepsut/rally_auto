package com.vmware.rally.automation.data.command;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;

/**
 * RCreateCaseCommand handles creation of TestCase object in Rally. 
 * Implements RCommand interface.
 * @author akaramyan
 */
public class RCreateCaseCommand implements RCommand {
	
	private String _name = "";
	private String _type = "";
	private String _method = "";

	public RCreateCaseCommand(String name, String type, String method) {
		setName(name);
		setType(type);
		setMethod(method);
	}

	public JsonObject execute() {
		if (!isValid()) {
			// TODO: throw exception
			return null;
		}
		
		JsonObject result = null;
		
		try {
			result = RallyManager.getInstance().createTestCase(_name, _type, _method);
		} catch (IOException exception) {
			// TODO: handle
		}
		
        return result;
	}
	
	public boolean isValid() {
		return !(_name.isEmpty());
	}
	
	
	/* Getters and Setters */
	
	public String getName() {
		return _name;
	}
	public void setName(String name) {
		_name = name;
	}
	
	public String getType() {
		return _type;
	}
	public void setType(String type) {
		_type = type;
	}
	
	public String getMethod() {
		return _method;
	}
	public void setMethod(String method) {
		_method = method;
	}
	
}
