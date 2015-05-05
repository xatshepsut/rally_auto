package com.vmware.rally.automation.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TestCase annotation.
 * Runtime, applies only to methods.
 * @author akaramyan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface TestCase {
	String id();
	String buildNumber();
	String testSetId();
}
