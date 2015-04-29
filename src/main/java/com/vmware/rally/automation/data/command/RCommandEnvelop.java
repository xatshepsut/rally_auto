package com.vmware.rally.automation.data.command;

public class RCommandEnvelop {
	private final RCommand _command;
	private String _key;
	
	public RCommand getCommand() {
		return _command; 
	}
	public String getKey() {
		return _key; 
	}
	
	public RCommandEnvelop(RCommand command, String key) {
		_command = command;
		_key = key;
	}
}


