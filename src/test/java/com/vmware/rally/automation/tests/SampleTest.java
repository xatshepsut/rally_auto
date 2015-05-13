package com.vmware.rally.automation.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.rally.automation.data.annotation.TestCase;


public class SampleTest {

	@TestCase(id = "TC11", buildNumber = "2242", testSetId = "TS1")
	@Test()
	public void test1() {
		boolean value = true;
		Assert.assertEquals(value, value);
	}
	
	@Test()
	@TestCase(id = "TC21", buildNumber = "2242", testSetId = "TS1")
	public void test2() {
		boolean value = true;
		Assert.assertEquals(value, value);
	}
	
}
