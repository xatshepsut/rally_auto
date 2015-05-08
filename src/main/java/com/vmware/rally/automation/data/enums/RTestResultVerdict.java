package com.vmware.rally.automation.data.enums;

public enum RTestResultVerdict {
	BLOCKED("Blocked"), 
	ERROR("Error"), 
	FAIL("Fail"), 
	INCONCLUSIVE("Inconclusive"), 
	PASS("Pass");

    private final String text;

    private RTestResultVerdict(final String text) {
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
