package com.vmware.rally.automation.data.command;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.controller.RallyManager;
import com.vmware.rally.automation.exception.InvalidRCommandException;
import com.vmware.rally.automation.exception.RTaskException;
import com.vmware.rally.automation.exception.UninitializedRallyApiException;

/**
 * RGetCommand handles retrieving of objects from Rally. 
 * Implements RCommand interface.
 * @author akaramyan
 */
public class RGetCommand implements RCommand {
	
	public enum RGetCommandType {
		GET_TEST_CASE, GET_TEST_SET;
	}
	
	private String _id;
	private RGetCommandType _type;
	
	public RGetCommand(String id, RGetCommandType type) {
		setId(id);
		setType(type);
	}

	public JsonObject execute() throws RTaskException, UninitializedRallyApiException, InvalidRCommandException {
		if (!isValid()) {
			throw new InvalidRCommandException();
		}
		
		JsonObject result = null;

		if (RGetCommandType.GET_TEST_CASE == _type) {
			result = RallyManager.getInstance().getTestCaseWithId(_id);
		} else if (RGetCommandType.GET_TEST_SET == _type) {
			result = RallyManager.getInstance().getTestSetWithId(_id);
		}
		
        return result;
	}
	
	public boolean isValid() {
		return !((_id == null || _id.isEmpty()) || _type == null);
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
