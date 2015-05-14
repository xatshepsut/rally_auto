package com.vmware.rally.automation.listener;

import java.lang.reflect.Method;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.vmware.rally.automation.controller.AutomationManager;
import com.vmware.rally.automation.data.RTestData;
import com.vmware.rally.automation.data.annotation.TestCase;
import com.vmware.rally.automation.data.enums.RTestResultVerdict;
import com.vmware.rally.automation.utils.LoggerWrapper;
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
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		LoggerWrapper.getInstance().logInfo("Started running test method "  + methodName);
		
		Method method = result.getMethod().getConstructorOrMethod().getMethod();
		TestCase tcAnnotation = method.getAnnotation(TestCase.class);
		
		if (tcAnnotation != null) {
			RTestData testCaseData = 
					new RTestData(tcAnnotation.id(), tcAnnotation.buildNumber(), tcAnnotation.testSetId());
			AutomationManager.getInstance().addTestData(testCaseData, methodName);
		}
	}

	/**
	 * Invoked each time a test succeeds.
	 */
	public void onTestSuccess(ITestResult result) {
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		LoggerWrapper.getInstance().logInfo("Test method "  + methodName + " passed");
		
	    AutomationManager.getInstance().onFinishedTestWithVerdict(methodName, RTestResultVerdict.PASS);
	}

	/**
	 * Invoked each time a test fails.
	 */
	public void onTestFailure(ITestResult result) {
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		LoggerWrapper.getInstance().logInfo("Test method "  + methodName + " failed");
		
		AutomationManager.getInstance().onFinishedTestWithVerdict(methodName, RTestResultVerdict.FAIL);
	}

	/**
	 * Invoked each time a test is skipped.
	 */
	public void onTestSkipped(ITestResult result) {
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		LoggerWrapper.getInstance().logInfo("Test method "  + methodName + " is skipped");
	}

	/**
	 * Invoked each time a method fails but has been annotated with successPercentage 
	 * and this failure still keeps it within the success percentage requested.
	 */
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		String methodName = TestUtils.getMethodFullName(result.getMethod());
		LoggerWrapper.getInstance().logInfo("Test method "  + methodName + " failed with sucess percantage.");
	}

	/**
	 * Invoked after the test class is instantiated and before any configuration method is called.
	 */
	public void onStart(ITestContext context) {
		LoggerWrapper.getInstance().registerLogger();
		LoggerWrapper.getInstance().logInfo("Started running test "  + context.getName());
	}

	/**
	 * Invoked after all the tests have run and all their Configuration methods have been called.
	 */
	public void onFinish(ITestContext context) {
		LoggerWrapper.getInstance().logInfo("Finished running test "  + context.getName());
		
		AutomationManager.getInstance().onComplete();
	}


}