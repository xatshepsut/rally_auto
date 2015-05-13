package com.vmware.rally.automation.exception;

public class RTaskException extends Exception {
	private static final long serialVersionUID = 1L;
	
	
	private String _message;
	
	public enum RTaskType {
		GET_TEST_CASE, GET_TEST_SET, GET_USER,
		CREATE_TEST_CASE, CREATE_TEST_CASE_RESULT;
	}
	
	public RTaskException() {
		super();
		_message = "Error making REST request.";
    }

	private RTaskException(RTaskType taskType) {
		super();
		
		_message = "";
		
		switch (taskType) {
		case GET_TEST_CASE:
			_message += "Error querying TestCase";
			break;
		case GET_TEST_SET:
			_message += "Error querying TestSet";
			break;
		case GET_USER:
			_message += "Error querying User";
			break;
		case CREATE_TEST_CASE:
			_message += "Error creating TestCase";
			break;
		case CREATE_TEST_CASE_RESULT:
			_message += "Error creating TestCaseResult";
			break;
		default:
			break;
		}
    }
	
	public RTaskException(RTaskType taskType, String additionalInfo) {
		this(taskType);
		_message += !additionalInfo.isEmpty() ? (", " + additionalInfo) : ".";
    }
	
	public RTaskException(RTaskType taskType, String[] errors) {
		this(taskType);
		_message += errors;
    }

	
	@Override
	public String getMessage() {
		return _message;
	}
}