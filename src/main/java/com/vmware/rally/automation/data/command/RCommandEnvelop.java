package com.vmware.rally.automation.data.command;

/**
 * Envelop class containing RCommand and key as metadata.
 * @author akaramyan
 */
public class RCommandEnvelop {
	
	private RCommand _command;
	private String _key;
	
	public RCommandEnvelop(RCommand command, String key) {
		setCommand(command);
		setKey(key);
	}
	
	
	/* Getters and Setters */
	
	public RCommand getCommand() {
		return _command; 
	}
	public void setCommand(RCommand command) {
		_command = command; 
	}
	
	public String getKey() {
		return _key; 
	}
	public void setKey(String key) {
		_key = key; 
	}
	
}


