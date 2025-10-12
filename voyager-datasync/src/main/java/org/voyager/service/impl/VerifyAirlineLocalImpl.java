package org.voyager.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.service.VerifyAirline;
import org.voyager.utils.ConstantsDatasync;
import java.util.Set;

import static org.voyager.utils.ConstantsDatasync.*;

public class VerifyAirlineLocalImpl implements VerifyAirline {
    private static int limit;
    private static int allDelta,deltaCurrentSize,deltaHubSize,deltaFormerSize,deltaFutureSize,deltaSeasonalSize,deltaFocusSize;
    private static Set<String> all,deltaCurrent,deltaHub,deltaFormer,deltaFuture,deltaSeasonal,deltaFocus;
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyAirlineLocalImpl.class);

    public VerifyAirlineLocalImpl(int processLimit) {
        limit = processLimit;
        loadCodesToProcess();
        LOGGER.info(String.format("loaded all delta codes: %d, delta current codes: %d, delta hub codes: %s, former delta codes: %d, future delta codes: %d, focus delta codes: %d",
                all.size(), deltaCurrent.size(),deltaHub.size(),deltaFormer.size(),deltaFuture.size(),deltaFocus.size()));
        filterProcessed();
        allDelta = all.size();
        LOGGER.info(String.format("after filtering, remaining codes to process: %d, process limit: %d",allDelta,limit));
    }

    @Override
    public void run() {
        processRemaining();
        saveProcessed();
    }

    @Override
    public void loadCodesToProcess() {
        all = ConstantsDatasync.loadCodesFromListFile(DELTA_ALL_FILE);

        deltaCurrent = ConstantsDatasync.loadCodesFromListFile(DELTA_CURRENT_FILE);
        deltaCurrentSize = deltaCurrent.size();

        deltaHub = ConstantsDatasync.loadCodesFromListFile(DELTA_HUB_FILE);
        deltaHubSize = deltaHub.size();

        deltaFormer = ConstantsDatasync.loadCodesFromListFile(DELTA_FORMER_FILE);
        deltaFormerSize = deltaFormer.size();

        deltaFuture = ConstantsDatasync.loadCodesFromListFile(DELTA_FUTURE_FILE);
        deltaFutureSize = deltaFuture.size();

        deltaSeasonal = ConstantsDatasync.loadCodesFromListFile(DELTA_SEASONAL_FILE);
        deltaSeasonalSize = deltaSeasonal.size();

        deltaFocus = ConstantsDatasync.loadCodesFromListFile(DELTA_FOCUS_FILE);
        deltaFocusSize = deltaFocus.size();
    }

    @Override
    public void filterProcessed() {
        deltaCurrent.forEach(all::remove);
        deltaHub.forEach(all::remove);
        deltaFormer.forEach(all::remove);
        deltaFuture.forEach(all::remove);
        deltaSeasonal.forEach(all::remove);
        deltaFocus.forEach(all::remove);
    }

    @Override
    public void processRemaining() {
        deltaHub.forEach(deltaCurrent::add);
        deltaSeasonal.forEach(deltaCurrent::add);
        deltaFocus.forEach(deltaCurrent::add);
    }

    @Override
    public void saveProcessed() {
        ConstantsDatasync.writeSetToFileForDBInsertion(deltaCurrent, DELTA_ALL_DB);
        ConstantsDatasync.writeSetToFileForDBInsertion(deltaFormer, DELTA_FORMER_DB);
        ConstantsDatasync.writeSetToFile(deltaCurrent, DELTA_ALL_FILE);
        ConstantsDatasync.writeSetToFile(deltaFormer, DELTA_FORMER_FILE);
        ConstantsDatasync.writeSetToFile(deltaHub, DELTA_HUB_FILE);
        ConstantsDatasync.writeSetToFile(deltaHub, DELTA_HUB_FILE);
        ConstantsDatasync.writeSetToFile(deltaFuture, DELTA_FUTURE_FILE);
        ConstantsDatasync.writeSetToFile(deltaSeasonal, DELTA_SEASONAL_FILE);
        ConstantsDatasync.writeSetToFile(deltaFocus, DELTA_FOCUS_FILE);
    }
}
