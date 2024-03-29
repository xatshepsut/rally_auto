package com.vmware.rally.automation.utils;

import org.testng.ITestNGMethod;

/**
 * TestUtils provides helpful methods for working with TestNG classes.
 * @author akaramyan
 */
public class TestUtils {
	
	/**
	 * Method for getting unique method name containing class full path
	 * @param method   - ITestNGMethod
	 * @return full method name including class path
	 */
	public static String getMethodFullName(ITestNGMethod method) {
		return method != null ? method.getRealClass().getName() + "." + method.getMethodName() : "";
	}
	
}
