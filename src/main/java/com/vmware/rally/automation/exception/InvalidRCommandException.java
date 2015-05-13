package com.vmware.rally.automation.exception;

public class InvalidRCommandException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final static String _message = "Attempt to execute invalid command.";

	public InvalidRCommandException() {
		super(_message);
    }
}