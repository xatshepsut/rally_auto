package com.vmware.rally.automation.listener;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.vmware.rally.automation.controller.AutomationManager;
import com.vmware.rally.automation.data.annotation.*;
import com.vmware.rally.automation.utils.TestUtils;

/**
 * Listener class for test execution results.
 * Implements ITestListener
 * @author akaramyan
 */
public class RTestListener implements ITestListener {
	
	/**
	 * Invoked each time before a test will be invoked.
	 * The ITestResult is only partially filled with the references to class, method, start millis and status.
	 */
	public void onTestStart(ITestResult result) {
		System.out.println("Test started: "  + result.getMethod().getMethodName());
		
		TestCase tcAnnotation = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(TestCase.class);
		if (tcAnnotation != null) {
			System.out.println("Test case id: " + tcAnnotation.id());
		}
		
		System.out.println();
	}

	/**
	 * Invoked each time a test succeeds.
	 */
	public void onTestSuccess(ITestResult result) {
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		
		System.out.println("Test succeded: "  + methodName);
	    System.out.println("Test case id: " + AutomationManager.getInstance().getTestDataWithKey(methodName).getId());
	    System.out.println();
	    
	    AutomationManager.getInstance().onFinishedTestWithKey(methodName, "Pass");
	}

	/**
	 * Invoked each time a test fails.
	 */
	public void onTestFailure(ITestResult result) {
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		
		System.out.println("Test failed: "  + methodName);
		System.out.println("Test case id: " + AutomationManager.getInstance().getTestDataWithKey(methodName).getId());
		System.out.println();
		
		AutomationManager.getInstance().onFinishedTestWithKey(methodName, "Fail");
	}

	/**
	 * Invoked each time a test is skipped.
	 */
	public void onTestSkipped(ITestResult result) {
		System.out.println("Test skipped: "  + result.getMethod().getMethodName());
		System.out.println();
	}

	/**
	 * Invoked each time a method fails but has been annotated with successPercentage 
	 * and this failure still keeps it within the success percentage requested.
	 */
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		System.out.println("Test failed with success percentage: "  + result.getMethod().getMethodName());
		System.out.println();
	}

	/**
	 * Invoked after the test class is instantiated and before any configuration method is called.
	 */
	public void onStart(ITestContext context) {
		System.out.println("Test suit started: " + context.getName());
		System.out.println();
	}

	/**
	 * Invoked after all the tests have run and all their Configuration methods have been called.
	 */
	public void onFinish(ITestContext context) {
		System.out.println("Test suit finished: " + context.getName());
		System.out.println();
		
		AutomationManager.getInstance().onFinishedTestSuite();
	}


}