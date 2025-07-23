package org.voyager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.airline.AirlineSync;
import org.voyager.airline.FlightsSync;
import org.voyager.airline.RoutesSync;
import org.voyager.model.SyncStep;
import org.voyager.utils.DatasyncProgramArguments;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class AirlineFullSync {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineFullSync.class);

    public static void main(String[] args) {
        LOGGER.info("printing from routes sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        List<SyncStep> stepList = datasyncProgramArguments.getStepList();
        if (stepList.contains(SyncStep.ROUTES_SYNC)) runRoutesSync(datasyncProgramArguments);
        if (stepList.contains(SyncStep.FLIGHTS_SYNC)) runFlightsSync(datasyncProgramArguments);
        if (stepList.contains(SyncStep.AIRLINE_SYNC)) runAirlineSync(datasyncProgramArguments);
    }

    private static void runAirlineSync(DatasyncProgramArguments datasyncProgramArguments) {
        datasyncProgramArguments.setThreadCountMax(1000);
        String[] args = datasyncProgramArguments.toArgs();
        StringJoiner stringJoiner = new StringJoiner(" ");
        Arrays.stream(args).forEach(stringJoiner::add);
        LOGGER.info(String.format("running AirlineSync with args: %s",stringJoiner));
        AirlineSync.main(args);
    }

    private static void runFlightsSync(DatasyncProgramArguments datasyncProgramArguments) {
        datasyncProgramArguments.setThreadCountMax(3);
        String[] args = datasyncProgramArguments.toArgs();
        StringJoiner stringJoiner = new StringJoiner(" ");
        Arrays.stream(args).forEach(stringJoiner::add);
        LOGGER.info(String.format("running FlightsSync with args: %s",stringJoiner));
        FlightsSync.main(args);
    }

    private static void runRoutesSync(DatasyncProgramArguments datasyncProgramArguments) {
        datasyncProgramArguments.setThreadCountMax(3);
        String[] args = datasyncProgramArguments.toArgs();
        StringJoiner stringJoiner = new StringJoiner(" ");
        Arrays.stream(args).forEach(stringJoiner::add);
        LOGGER.info(String.format("running RoutesSync with args: %s",stringJoiner));
        RoutesSync.main(args);
    }
}
