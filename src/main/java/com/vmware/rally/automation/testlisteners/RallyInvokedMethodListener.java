package com.vmware.rally.automation.testlisteners;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import com.vmware.rally.automation.annotations.*;
import com.vmware.rally.automation.data.RallyTestCase;
import com.vmware.rally.automation.utils.TestUtils;


public class RallyInvokedMethodListener implements IInvokedMethodListener    {

	private static final Map<String, RallyTestCase> _testCaseMap = new HashMap<String, RallyTestCase>();

	public static RallyTestCase getTestCaseForMethod(String methodName) {
		if (_testCaseMap.containsKey(methodName)) {
			return _testCaseMap.get(methodName);
		}
		
		return null;
	}

	/*
	 * IInvokedMethodListener interface implementation
	 */
	
	public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
//		System.out.println("Before invocation of " + method.getTestMethod().getMethodName());

		if (invokedMethod.isTestMethod()) {
			Method method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
			
			
			TestCase tcAnnotation = method.getAnnotation(TestCase.class);
			if (tcAnnotation != null) {
				
				RallyTestCase rtc = new RallyTestCase(tcAnnotation.id(), tcAnnotation.buildNumber(), tcAnnotation.testSetId());
				_testCaseMap.put(TestUtils.getMethodFullName(invokedMethod.getTestMethod()), rtc);
				
			} else {
//				// TODO: new test case  
//				NewTestCase ntcAnnotation = method.getAnnotation(NewTestCase.class);
//				if (ntcAnnotation != null) {
//					rtc.setFormattedId(ntcAnnotation.name());
//					rtc.setBuildNumber(ntcAnnotation.buildNumber());
//				} else {
//					return;
//				}
			}	
		}
	}
	
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
//		System.out.println("After invocation of " + method.getTestMethod().getMethodName());
	}
	
}