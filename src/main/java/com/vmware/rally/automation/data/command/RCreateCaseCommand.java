package com.vmware.rally.automation.data.command;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;

public class RCreateCaseCommand implements RCommand {
	private String _name;
	
	public RCreateCaseCommand(String name) {
		_name = name;
	}

	public JsonObject execute() {
		JsonObject result = null;
		
		try {
			result = RallyManager.getInstance().createTestCase(_name);
		} catch (IOException exception) {
			// handle exception, print error etc
		}
		
        return result;
	}
	
}
