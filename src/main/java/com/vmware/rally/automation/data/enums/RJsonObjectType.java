package com.vmware.rally.automation.data.enums;

public enum RJsonObjectType {
	JSON_OBJECT_TESTCASE("TestCase"), 
	JSON_OBJECT_TESTCASERESULT("TestCaseResult"),
	JSON_OBJECT_TESTSET("TestSet"),
	JSON_OBJECT_USER("User");
	
    private final String text;

    private RJsonObjectType(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
