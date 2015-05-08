package com.vmware.rally.automation.listener;

import java.lang.reflect.Method;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import com.vmware.rally.automation.controller.AutomationManager;
import com.vmware.rally.automation.data.RTestData;
import com.vmware.rally.automation.data.annotation.TestCase;
import com.vmware.rally.automation.utils.TestUtils;

/**
 * Listener class for test method invocation.
 * Implements IInvokedMethodListener.
 * @author akaramyan
 */
public class RInvokedMethodListener implements IInvokedMethodListener    {
	
	public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
		//System.out.println("Before invocation of " + invokedMethod.getTestMethod().getMethodName());

		if (invokedMethod.isTestMethod()) {
			Method method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
			TestCase tcAnnotation = method.getAnnotation(TestCase.class);
			
			if (tcAnnotation != null) {
				String methodName = TestUtils.getMethodFullName(invokedMethod.getTestMethod());
				RTestData testCaseData = 
						new RTestData(tcAnnotation.id(), tcAnnotation.buildNumber(), tcAnnotation.testSetId());
				AutomationManager.getInstance().addTestData(testCaseData, methodName);
			}
		}
	}
	
	public void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
		//System.out.println("After invocation of " + invokedMethod.getTestMethod().getMethodName());
	}
	
}