package org.transitclock.integration_tests;

/**
 * The results of an integration test.
 */
public class IntegrationTestResult {
    private int returnCode = -99;
    private String output = null;

    public IntegrationTestResult(int rc) {
        this.returnCode = rc;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
