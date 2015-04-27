package com.vmware.rally.automation.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.rally.automation.annotations.TestCase;


public class SampleTest {

	@Test()
	@TestCase(id = "TC1", buildNumber = "2234")
	public void test1() {
		boolean value = true;
		Assert.assertEquals(value, value);
	}
	
	@Test()
	@TestCase(id = "TC2", buildNumber = "2234")
	public void test2() {
		boolean value = true;
		Assert.assertEquals(value, value);
	}
	
}
