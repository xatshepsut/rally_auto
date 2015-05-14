package com.vmware.rally.automation.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import java.util.Map;
import java.util.HashMap;

/**
 * Wrapper class for logger. Currently wraps Log4j logger.
 * @author akaramyan
 */
public class LoggerWrapper {

	private Map<String, Logger> _loggerMap = new HashMap<String, Logger>();
	
	/* Constructor */
	
	private LoggerWrapper() {
	}
	
	private static final LoggerWrapper _instance = new LoggerWrapper();
	public static LoggerWrapper getInstance() {
		return _instance;
	}
	
	
	/* Public Methods */
	
	public boolean registerLogger() {
		String callerClassName = getCallerClassName();
		if (_loggerMap.containsKey(callerClassName)) {
			return false;
		}
		
		Logger logger = Logger.getLogger(callerClassName);
		_loggerMap.put(callerClassName, logger);
		return true;
	}
	
	public boolean logError(String message) {
		Logger logger = getLoggerForCallerClass();
		if (logger == null) {
			return false;
		}
		
		log(logger, Level.ERROR, message);
		return true;
	}
	
	public boolean logInfo(String message) {
		Logger logger = getLoggerForCallerClass();
		if (logger == null) {
			return false;
		}
		
		log(logger, Level.INFO, message);
		return true;
	}
	
	public boolean logWarning(String message) {
		Logger logger = getLoggerForCallerClass();
		if (logger == null) {
			return false;
		}
		
		log(logger, Level.WARN, message);
		return true;
	}
	
	
	/* Private Methods */
	
	private Logger getLoggerForCallerClass() {
		return _loggerMap.get(getCallerClassName());
	}
	
	private String getCallerClassName() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();

		String thisClassName = this.getClass().getName();
		String callerClassName = "";
		
		for (int i = 1; i < trace.length; i++) {
			callerClassName = trace[i].getClassName();
			if (callerClassName != thisClassName) {
				break;
			}
		}
		
		return callerClassName;
	}
	
	private void log(Logger logger, Level logLevel, String message) {
		assert logger != null;
		logger.log(LoggerWrapper.class.getCanonicalName(), logLevel, message, null);
	}

}
