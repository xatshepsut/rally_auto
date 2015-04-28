package com.vmware.rally.automation.testlisteners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.vmware.rally.automation.annotations.*;
import com.vmware.rally.automation.utils.TestUtils;


public class RallyTestListener implements ITestListener {
	
	/*
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
		
		
		// TODO: move get annotation code here
		// TODO: create pack
		// TODO: send pack to rally test manager -> run initial queries and init TestCase object
	}

	/*
	 * Invoked each time a test succeeds.
	 */
	public void onTestSuccess(ITestResult result) {
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		
		System.out.println("Test succeded: "  + methodName);
	    System.out.println("Test case id: " + RallyInvokedMethodListener.getTestCaseForMethod(methodName).getId());
	    System.out.println();
	}

	/*
	 * Invoked each time a test fails.
	 */
	public void onTestFailure(ITestResult result) {
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		
		System.out.println("Test failed: "  + methodName);
		System.out.println("Test case id: " + RallyInvokedMethodListener.getTestCaseForMethod(methodName).getId());
		System.out.println();
	}
	
	public void onTestResult() {
		// TODO: send pack to rally test manager -> run initial queries and init TestCase object
	}

	/*
	 * Invoked each time a test is skipped.
	 */
	public void onTestSkipped(ITestResult result) {
		System.out.println("Test skipped: "  + result.getMethod().getMethodName());
		System.out.println();
	}

	/*
	 * Invoked each time a method fails but has been annotated with successPercentage 
	 * and this failure still keeps it within the success percentage requested.
	 */
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		System.out.println("Test failed with success percentage: "  + result.getMethod().getMethodName());
		System.out.println();
	}

	/*
	 * Invoked after the test class is instantiated and before any configuration method is called.
	 */
	public void onStart(ITestContext context) {
		System.out.println("Test suit started: " + context.getName());
		System.out.println();
		
		
		// TODO: create singleton rally task manager
	}

	/*
	 * Invoked after all the tests have run and all their Configuration methods have been called.
	 */
	public void onFinish(ITestContext context) {
		System.out.println("Test suit finished: " + context.getName());
		System.out.println();
	}


}