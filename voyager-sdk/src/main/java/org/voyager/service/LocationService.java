package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.country.Continent;
import org.voyager.model.location.*;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;
import java.util.StringJoiner;

import static org.voyager.utils.ConstantsUtils.*;

public class LocationService {
    private static final String LOCATIONS_PATH = "/locations";
    private static final String LOCATION_PATH = "/location";
    private final ServiceUtils serviceUtils;

    LocationService() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    LocationService(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    public Either<ServiceError,List<Location>> getLocations() {
        return serviceUtils.fetch(LOCATIONS_PATH,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,Location> getLocation(Source source, String sourceId) {
        String requestURL = LOCATION_PATH.concat(String.format("?%s=%s" + "&%s=%s",
                SOURCE_PARAM_NAME,source.name(),SOURCE_ID_PARAM_NAME,sourceId));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Location.class);
    }

    public Either<ServiceError,List<Location>> getLocations(Source source, Continent continent) {
        String requestURL = LOCATIONS_PATH.concat(String.format("?%s=%s" + "&%s=%s",
                SOURCE_PARAM_NAME,source.name(), CONTINENT_PARAM_NAME,continent.name()));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,List<Location>> getLocations(Source source, Continent continent,List<Status> statusList) {
        StringJoiner statusJoiner = new StringJoiner(",");
        statusList.forEach(status -> statusJoiner.add(status.name()));
        String requestURL = LOCATIONS_PATH.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s",
                SOURCE_PARAM_NAME,source.name(), CONTINENT_PARAM_NAME,continent.name(),LOCATION_STATUS_PARAM_NAME,statusJoiner));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,List<Location>> getLocations(Status status) {
        String requestURL = LOCATIONS_PATH.concat(String.format("?%s=%s",
                LOCATION_STATUS_PARAM_NAME,status.name()));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,List<Location>> getLocations(Integer limit) {
        String requestURL = LOCATIONS_PATH.concat(String.format("?%s=%d",
                LIMIT_PARAM_NAME,limit));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }


    public Either<ServiceError,Location> getLocation(Integer id) {
        String requestURL = LOCATION_PATH.concat(String.format("/%d",id));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Location.class);
    }

    public Either<ServiceError,Boolean> deleteLocation(Integer id) {
        String requestURL = LOCATIONS_PATH.concat(String.format("/%d",id));
        return serviceUtils.fetchNoResponseBody(requestURL,HttpMethod.DELETE);
    }

    public Either<ServiceError,Location> createLocation(LocationForm locationForm) {
        return serviceUtils.fetchWithRequestBody(LOCATIONS_PATH,HttpMethod.POST,Location.class,locationForm);
    }

    public Either<ServiceError,Location> patchLocation(Integer id, LocationPatch locationPatch) {
        String requestURL = LOCATIONS_PATH.concat(String.format("/%d",id));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Location.class,locationPatch);
    }
}
