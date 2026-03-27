package org.voyager.sync.service.impl;

import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
import org.voyager.commons.model.airline.AirlineBatchUpsertResult;
import org.voyager.sdk.service.AirlineService;
import org.voyager.sync.config.FlightSyncConfig;
import org.voyager.sync.service.AirlineAirportProcessor;
import org.voyager.sync.utils.ConstantsDatasync;
import java.io.FileWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class AirlineAirportProcessorImpl implements AirlineAirportProcessor {
    private final AirlineService airlineService;
    private final FileWriter retryAirlineFileWriter;
    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineAirportProcessorImpl.class);
    AirlineAirportProcessorImpl(AirlineService airlineService, FlightSyncConfig flightSyncConfig) {
        this.airlineService = airlineService;
        this.retryAirlineFileWriter = flightSyncConfig.getRetryAirlineFileWriter();
    }

    @Override
    public void process(Map<Airline, Set<String>> airlineMap) {
        if (airlineMap.isEmpty()) {
            LOGGER.info("no airlines in process map, skipping airline batch updates");
            return;
        }
        LOGGER.info("processing deactivate and upsert airline map of {} total airlines", airlineMap.size());
        Map<Airline, List<String>> failedAirlineAirports = new HashMap<>();
        airlineMap.forEach((airline,iataCodes) ->{
            deactivateAirline(airline);
            batchUpsertAirlineAirports(airline,iataCodes,failedAirlineAirports);
        });
        if (!failedAirlineAirports.isEmpty()) {
            LOGGER.info("writing failed airline airports to retry file");
            ConstantsDatasync.writeAirlineListToFile(failedAirlineAirports, retryAirlineFileWriter);
        }
    }

    private void batchUpsertAirlineAirports(
            Airline airline, Set<String> iataCodes, Map<Airline, List<String>> failedAirlineAirports) {
        LOGGER.info("upserting airline {} with {} airport codes",
                airline.name(),iataCodes.size());
        AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder().airline(airline.name())
                .isActive(true).iataList(new ArrayList<>(iataCodes)).build();
        Either<ServiceError, AirlineBatchUpsertResult> either = airlineService.batchUpsert(airlineBatchUpsert);
        if (either.isLeft()) {
            LOGGER.error("failed to batch UPSERT airline {} with codes: {}, error: {}",
                    airline,iataCodes,either.getLeft().getException().getMessage());
            failedAirlineAirports.put(airline,new ArrayList<>(iataCodes));
        } else {
            AirlineBatchUpsertResult airlineBatchUpsertResult = either.get();
            LOGGER.info("successful batch UPSERT airline {} with {} records created, {} records skipped, {} records updated",
                    airline,airlineBatchUpsertResult.getCreatedCount(),
                    airlineBatchUpsertResult.getSkippedCount(),
                    airlineBatchUpsertResult.getUpdatedCount());
        }
        LOGGER.info("-----------------");
    }

    private void deactivateAirline(Airline airline) {
        LOGGER.info("deactivating {} airline airports",airline.name());
        // deactivate airline
        Either<ServiceError, Integer> deleteEither = airlineService.deactivateAirline(airline);
        if (deleteEither.isRight()) {
            LOGGER.info("successfully deactivated {} airline of {} airports",
                    airline.name(),deleteEither.get());
        } else {
            LOGGER.error("failed to bdeactivate {} airline, error: {}",
                    airline,deleteEither.getLeft().getException().getMessage());
        }
    }
}
