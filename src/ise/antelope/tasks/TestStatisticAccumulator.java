package ise.antelope.tasks;

interface TestStatisticAccumulator {
    public int getFailedCount();
    public int getWarningCount();
    public int getPassedCount();
    public int getRanCount();
    public String getSummary();
    public int getTestCaseCount();
}
