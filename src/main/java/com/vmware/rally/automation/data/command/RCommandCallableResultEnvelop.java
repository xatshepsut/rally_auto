package com.vmware.rally.automation.data.command;

import java.util.concurrent.Future;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.data.enums.RJsonObjectType;

/**
 * Envelop class containing RCommandCallable and JSON object type as metadata.
 * @author akaramyan
 */
public class RCommandCallableResultEnvelop {
	
	private Future<JsonObject> _future;
	private RJsonObjectType _type;
	
	public RCommandCallableResultEnvelop(Future<JsonObject> jsonObject, RJsonObjectType type) {
		setFuture(jsonObject);
		setType(type);
	}
	
	
	/* Getters and Setters */
	
	public Future<JsonObject> getFuture() {
		return _future; 
	}
	public void setFuture(Future<JsonObject> future) {
		_future = future; 
	}
	
	public RJsonObjectType getType() {
		return _type; 
	}
	public void setType(RJsonObjectType type) {
		_type = type; 
	}
	
}


