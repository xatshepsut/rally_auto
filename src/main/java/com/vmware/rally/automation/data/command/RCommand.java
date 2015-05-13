package com.vmware.rally.automation.data.command;

import com.google.gson.JsonObject;
import com.vmware.rally.automation.exception.InvalidRCommandException;
import com.vmware.rally.automation.exception.RTaskException;
import com.vmware.rally.automation.exception.UninitializedRallyApiException;

/**
 * RCommand interface.
 * @author akaramyan
 */
public interface RCommand {
	
	/**
	 * Executes Rally REST command.
	 * @return Rally REST command JSON result
	 * @throws RTaskException
	 * @throws UninitializedRallyApiException
	 * @throws InvalidRCommandException
	 */
	public JsonObject execute() throws RTaskException, UninitializedRallyApiException, InvalidRCommandException;
	
	/**
	 * Checks if all necessary field for execution are set. 
	 * In case any of fields are missing command is considered invalid.
	 * @return <i>true</i> if command is valid, <i>false</i> otherwise
	 */
	public boolean isValid();
}
