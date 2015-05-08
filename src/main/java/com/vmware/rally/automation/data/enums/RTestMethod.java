package com.vmware.rally.automation.data.enums;

public enum RTestMethod {
	MANUAL("Manual"), 
	AUTOMATED("Automated");
	
    private final String text;

    private RTestMethod(final String text) {
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
