package com.vmware.rally.automation.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.rally.automation.data.annotation.TestCase;


public class SampleTest {

	@Test()
	@TestCase(id = "TC1", buildNumber = "2239", testSetId = "TS1")
	public void test1() {
		boolean value = true;
		Assert.assertEquals(value, !value);
	}
	
	@Test()
	@TestCase(id = "TC2", buildNumber = "2239", testSetId = "TS1")
	public void test2() {
		boolean value = true;
		Assert.assertEquals(value, value);
	}
	
}
