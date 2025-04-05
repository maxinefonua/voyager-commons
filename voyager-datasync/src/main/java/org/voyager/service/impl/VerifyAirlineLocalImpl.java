package org.voyager.service.impl;

import org.voyager.service.VerifyAirline;

public class VerifyAirlineLocalImpl implements VerifyAirline {
    private static int limit;
    public VerifyAirlineLocalImpl(int processLimit) {
        limit = processLimit;
//        loadCodesToProcess();
//        LOGGER.debug(String.format("loaded all codes: %d, civil codes: %d, military codes: %d, historical codes: %d, issue codes: %d, and special types: %d",
//                all.size(),civilStartSize,militaryStartSize,historicalStartSize,issueStartSize,specialTypeStartSize));
//        filterProcessed();
//        allToProcessSize = all.size();
//        LOGGER.info(String.format("after filtering, remaining codes to process: %d, process limit: %d",allToProcessSize,limit));
    }

    @Override
    public void run() {

    }

    @Override
    public void loadCodesToProcess() {

    }

    @Override
    public void filterProcessed() {

    }

    @Override
    public void processRemaining() {

    }

    @Override
    public void saveProcessed() {

    }
}
