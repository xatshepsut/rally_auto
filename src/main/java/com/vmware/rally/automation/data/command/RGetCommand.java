package com.vmware.rally.automation.data.command;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;

public class RGetCommand implements RCommand {
	public enum RGetCommandType {
		GET_TEST_CASE, GET_TEST_SET
	}
	
	private RGetCommandType _type;
	private String _id;
	
	public RGetCommand(String id, RGetCommandType type) {
		_id = id;
		_type = type;
	}

	public JsonObject execute() {
		JsonObject result = null;
		
		try {
			if (RGetCommandType.GET_TEST_CASE == _type) {
				result = RallyManager.getInstance().getTestCaseWithId(_id);
			} else if (RGetCommandType.GET_TEST_SET == _type) {
				result = RallyManager.getInstance().getTestSetWithId(_id);
			}
		} catch (IOException exception) {
			// handle exception, print error etc
		}
		
        return result;
	}
	
}
