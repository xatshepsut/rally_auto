package com.vmware.rally.automation.data.command;

import com.google.gson.JsonObject;

/**
 * RCommand interface.
 * @author akaramyan
 */
public interface RCommand {
	
	public JsonObject execute();
	
	public boolean isValid();
}
