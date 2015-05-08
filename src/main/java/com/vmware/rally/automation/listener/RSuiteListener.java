package com.vmware.rally.automation.listener;

import java.util.Iterator;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;

public class RSuiteListener implements ISuiteListener {
	
	/**
	 * This method is invoked before the SuiteRunner starts.
	 */
	public void onStart(ISuite suite) {
		System.out.println(suite.getName() + " attributes: " + suite.getAttributeNames());		
	}
	
	
	/**
	 * This method is invoked after the SuiteRunner has run all the test suites.
	 */
	public void onFinish(ISuite suite) {
		System.out.println(suite.getName() + " attributes: " + suite.getAttributeNames());
		
		for (Iterator<String> iterator = suite.getResults().keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			ITestContext testContext = suite.getResults().get(key).getTestContext();
			
			System.out.println(testContext.getAllTestMethods());
			System.out.println(testContext.getFailedTests());
			System.out.println(testContext.getPassedTests());
		}
		
	}

}
