package com.vmware.rally.automation.exception;

public class UninitializedRallyApiException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final static String _message = "Attempt to use RallyManager without initializing.";

	public UninitializedRallyApiException() {
		super(_message);
    }
}