package com.vmware.rally.automation.testlisteners;

import java.util.HashMap;
import java.util.Map;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import com.vmware.rally.automation.annotations.TestCase;
import com.vmware.rally.automation.data.RallyTestCase;


public class RallyInvokeMethodListener implements IInvokedMethodListener    {

	private static final Map<String, RallyTestCase> _testCaseMap = new HashMap<String, RallyTestCase>() ;

	public static RallyTestCase getTestCaseForMethod(String methodName) {
		if (_testCaseMap.containsKey(methodName)) {
			return _testCaseMap.get(methodName);
		}
		
		return null;
	}
	
	
	/*
	 * IInvokedMethodListener interface implementation
	 */
	
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
//		System.out.println("Before invocation of " + method.getTestMethod().getMethodName());

		if (method.isTestMethod()) {
			
			TestCase tcAnnotation = method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(TestCase.class);
			if (tcAnnotation != null) {
				RallyTestCase rtc = new RallyTestCase();
				rtc.setFormattedId(tcAnnotation.id());
				
				_testCaseMap.put(method.getTestMethod().getMethodName(), rtc);
			}
		}
	}
	
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
//		System.out.println("After invocation of " + method.getTestMethod().getMethodName());
	}
	
}