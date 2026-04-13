package org.voyager.sync.service.impl;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airport.*;
import org.voyager.commons.model.geoname.GeoPlace;
import org.voyager.commons.model.geoname.GeoTimezone;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import org.voyager.commons.model.response.PagedResponse;
import org.voyager.sdk.service.AirportService;
import org.voyager.sdk.service.GeoService;
import org.voyager.sync.model.chaviation.AirportCH;
import org.voyager.sync.model.flightradar.AirportFR;
import org.voyager.sync.model.flightradar.airport.AirportDetailsFR;
import org.voyager.sync.model.flightradar.airport.DetailsFR;
import org.voyager.sync.service.AirportReference;
import org.voyager.sync.service.external.ChAviationService;
import org.voyager.sync.service.external.FlightRadarService;

import java.util.*;

public class AirportReferenceImpl implements AirportReference {
    private final Set<String> airportCodes = new HashSet<>();
    private final Map<String,Airport> civilAirports = new HashMap<>();
    private final Map<String,AirportFR> missingAirports = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportReferenceImpl.class);
    private final AirportService airportService;

    public AirportReferenceImpl(AirportService airportService){
        this.airportService = airportService;
        loadAirports();
    }


    private void loadAirports() {
        // load all airport code set
        int initialAirportCodeSize = airportCodes.size();
        airportService.getIATACodes().fold(
                serviceError -> {
                    throw new RuntimeException(serviceError.getException().getMessage(),
                            serviceError.getException());
                },
                airportCodes::addAll
        );
        if (initialAirportCodeSize == 0) {
            LOGGER.info("voyager reference loaded {} codes in all airports set", airportCodes.size());
        } else {
            LOGGER.info("voyager reference refreshed with {} codes in all airports set from {} codes initially",
                    airportCodes.size(),initialAirportCodeSize);
        }

        // load civil codes set
        int initialCivilSize = civilAirports.size();
        AirportQuery airportQuery = AirportQuery.builder().airportTypeList(List.of(AirportType.CIVIL))
                .size(1000).page(0).build();
        Either<ServiceError, PagedResponse<Airport>> civilAirportsEither = airportService.getAirports(airportQuery);
        while (civilAirportsEither.isRight()) {
            PagedResponse<Airport> pagedResponse = civilAirportsEither.get();
            pagedResponse.getContent().forEach(airport ->
                    civilAirports.put(airport.getIata(),airport));
            if (pagedResponse.isLast()) {
                if (initialCivilSize == 0) {
                    LOGGER.info("voyager reference loaded {} airports into civil map",
                            civilAirports.size());
                } else {
                    LOGGER.info("voyager reference refreshed with {} airports into civil map from {} airports initially",
                            civilAirports.size(),initialCivilSize);
                }
                return;
            }
            airportQuery.setPage(airportQuery.getPage()+1);
            civilAirportsEither = airportService.getAirports(airportQuery);
        }
        Exception exception = civilAirportsEither.getLeft().getException();
        throw new RuntimeException(exception.getMessage(),exception);
    }

    @Override
    public void refreshReference(AirportService airportService) {
        loadAirports();
    }

    @Override
    public void addCivilAirport(Airport airport) {
        civilAirports.put(airport.getIata(),airport);
    }

    @Override
    public Either<ServiceError,Airport> addMissingAirport(String iata, AirportFR airportFR, GeoService geoService) {
        assert !isSavedAirport(iata);

        Either<ServiceError, AirportCH> chEither = ChAviationService.getAirportCH(iata);
        Either<ServiceError, Option<AirportDetailsFR>> detailsFREither =
                FlightRadarService.fetchAirportDetails(iata);

        Double latitude = airportFR.getLat();
        Double longitude = airportFR.getLon();
        String name = null;
        String countryCode = null;
        AirportType airportType = AirportType.UNVERIFIED;

        if (chEither.isRight()) {
            AirportCH airportCH = chEither.get();
            name = airportCH.getName();
            countryCode = airportCH.getCountryCode();
            if (latitude == null) latitude = airportCH.getLatitude();
            if (longitude == null) longitude = airportCH.getLongitude();
            airportType = airportCH.getType();
        }

        String city = null;
        String zoneId = null;
        if (detailsFREither.isRight() && detailsFREither.get().isDefined()) {
            DetailsFR detailsFR = detailsFREither.get().get().getDetails();
            if (StringUtils.isBlank(name)) name = detailsFR.getName();
            if (StringUtils.isBlank(countryCode) || !countryCode.matches(Regex.COUNTRY_CODE)) countryCode = detailsFR.getPosition().getCountry().getCode();
            if (latitude == null) latitude = detailsFR.getPosition().getLatitude();
            if (longitude == null) longitude = detailsFR.getPosition().getLongitude();
            city = detailsFR.getPosition().getRegion().getCity();
            zoneId = detailsFR.getTimezone().getZoneId();
        }

        String subdivision = null;
        GeoNearbyQuery geoNearbyQuery = GeoNearbyQuery.builder().latitude(latitude).longitude(longitude).build();
        Either<ServiceError, List<GeoPlace>> geoEither = geoService.findNearbyPlaces(geoNearbyQuery);
        if (geoEither.isRight() && !geoEither.get().isEmpty()) {
            GeoPlace geoPlace = geoEither.get().get(0);
            if (StringUtils.isBlank(city)) city = geoPlace.getName();
            if (StringUtils.isBlank(countryCode) || !countryCode.matches(Regex.COUNTRY_CODE)) countryCode = geoPlace.getCountryCode();
            subdivision = geoPlace.getAdminName1();
        }

        if (StringUtils.isBlank(zoneId)) {
            GeoTimezoneQuery geoTimezoneQuery = GeoTimezoneQuery.builder().latitude(latitude).longitude(longitude).build();
            Either<ServiceError, GeoTimezone> timezoneEither = geoService.getTimezone(geoTimezoneQuery);
            if (timezoneEither.isRight()) {
                zoneId = timezoneEither.get().getTimezoneId();
            }
        }

        if (StringUtils.isNotBlank(subdivision)) subdivision = subdivision.trim();
        if (StringUtils.isNotBlank(name)) name = name.trim();
        if (StringUtils.isNotBlank(city)) city = city.trim();

        AirportForm airportForm = AirportForm.builder().iata(iata).longitude(String.valueOf(longitude))
                .latitude(String.valueOf(latitude)).airportType(airportType.name()).zoneId(zoneId)
                .countryCode(countryCode).subdivision(subdivision).city(city).name(name)
                .build();

        Either<ServiceError, Airport> createEither = airportService.createAirport(airportForm);
        if (createEither.isRight()) {
            civilAirports.put(iata,createEither.get());
            airportCodes.add(iata);
        } else {
            missingAirports.put(iata,airportFR);
        }
        return createEither;
    }

    @Override
    public void addNonCivilAirport(String airportCode) {
        airportCodes.add(airportCode);
    }

    @Override
    public boolean isSavedAirport(String airportCode) {
        return airportCodes.contains(airportCode);
    }

    @Override
    public Option<Airport> getCivilAirportOption(String airportCode) {
        return Option.of(civilAirports.get(airportCode));
    }
}