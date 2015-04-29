package com.vmware.rally.automation.data.command;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;

public class RCreateCaseCommand implements RCommand {
	
	private String _name = "";

	public RCreateCaseCommand(String name) {
		setName(name);
	}

	public JsonObject execute() {
		if (!isValid()) {
			// TODO: throw exception
			return null;
		}
		
		JsonObject result = null;
		
		try {
			result = RallyManager.getInstance().createTestCase(_name);
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
	
}
