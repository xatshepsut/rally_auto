package com.vmware.rally.automation.tests;

import org.testng.Assert;
import org.testng.annotations.Test;


public class SampleTest {

	@Test()
	public void test1() {
		boolean value = true;
		Assert.assertEquals(value, !value);
	}
	
	@Test()
	public void test2() {
		boolean value = true;
		Assert.assertEquals(value, !value);
	}
	
}
