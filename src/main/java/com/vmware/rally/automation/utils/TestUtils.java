package com.vmware.rally.automation.utils;

import org.testng.ITestNGMethod;

public class TestUtils {
	
	/**
	 * Method for getting unique method name containing class full path
	 * 
	 * @param method
	 * @return method name with class path
	 */
	public static String getMethodFullName(ITestNGMethod method) {
		if (method == null) {
			return null;
		}
		
		return method.getRealClass().getName() + "." + method.getMethodName();
	}
	
}
