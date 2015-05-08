package com.vmware.rally.automation.data.enums;

public enum RTestType {
	ACCEPTANCE("Acceptance"), 
	FUNCTIONAL("Functional"),
	PERFORMANCE("Performance"),
	REGRESSION("Regression"),
	USABILITY("Usability"),
	USER_INTERFACE("User Interface");
	
    private final String text;

    private RTestType(final String text) {
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
