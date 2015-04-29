package com.vmware.rally.automation.data.command;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;

public class RGetCommand implements RCommand {
	
	public enum RGetCommandType {
		GET_TEST_CASE, GET_TEST_SET, NOP;
	}
	
	private String _id = "";
	private RGetCommandType _type = RGetCommandType.NOP;
	
	public RGetCommand(String id, RGetCommandType type) {
		setId(id);
		setType(type);
	}

	public JsonObject execute() {
		if (!isValid()) {
			// TODO: throw exception
			return null;
		}
		
		JsonObject result = null;
		
		try {
			if (RGetCommandType.GET_TEST_CASE == _type) {
				result = RallyManager.getInstance().getTestCaseWithId(_id);
			} else if (RGetCommandType.GET_TEST_SET == _type) {
				result = RallyManager.getInstance().getTestSetWithId(_id);
			}
		} catch (IOException exception) {
			// TODO: handle
		}
		
        return result;
	}
	
	public boolean isValid() {
		return !(_id.isEmpty());
	}
	
	
	/* Getters and Setters */
	
	public String getId() {
		return _id;
	}
	public void setId(String id) {
		_id = id;
	}

	public RGetCommandType getType() {
		return _type;
	}
	public void setType(RGetCommandType type) {
		_type = type;
	}
	
}
