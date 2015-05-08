package com.vmware.rally.automation.data.command;

import java.util.concurrent.Callable;

import com.google.gson.JsonObject;

/**
 * RCommandCallable is callable class that executes given RCommand.
 * Implements Callable. Returns JsonObject.
 * @author akaramyan
 */
public class RCommandCallable implements Callable<JsonObject> {
	private RCommand _command;
	
	public RCommandCallable(RCommand command) {
		_command = command;
	}
	
	public JsonObject call() throws Exception {
		JsonObject result = null;
		
		if (_command != null) {
			result = _command.execute();
		}
		
		return result;
	}
}