package org.voyager.service;

public interface VerifyAirline {
    public void run();
    public void loadCodesToProcess();
    public void filterProcessed();
    public void processRemaining();
    public void saveProcessed();
}
