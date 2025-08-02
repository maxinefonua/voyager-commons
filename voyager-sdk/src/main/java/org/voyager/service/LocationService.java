package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.country.Continent;
import org.voyager.model.location.*;

import java.util.List;
import java.util.StringJoiner;

import static org.voyager.service.Voyager.*;
import static org.voyager.utils.ConstantsUtils.*;

public class LocationService {
    private final String servicePath;
    private final String serviceLocationPath;

    LocationService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getLocationsPath();
        this.serviceLocationPath = voyagerConfig.getLocationPath();
    }

    public Either<ServiceError,List<Location>> getLocations() {
        return fetch(servicePath,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,Location> getLocation(Source source, String sourceId) {
        String requestURL = serviceLocationPath.concat(String.format("?%s=%s" + "&%s=%s",
                SOURCE_PARAM_NAME,source.name(),SOURCE_ID_PARAM_NAME,sourceId));
        return fetch(requestURL,HttpMethod.GET,Location.class);
    }

    public Either<ServiceError,List<Location>> getLocations(Source source, Continent continent) {
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s",
                SOURCE_PARAM_NAME,source.name(), CONTINENT_PARAM_NAME,continent.name()));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,List<Location>> getLocations(Source source, Continent continent,List<Status> statusList) {
        StringJoiner statusJoiner = new StringJoiner(",");
        statusList.forEach(status -> statusJoiner.add(status.name()));
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s" + "&%s=%s",
                SOURCE_PARAM_NAME,source.name(), CONTINENT_PARAM_NAME,continent.name(),LOCATION_STATUS_PARAM_NAME,statusJoiner));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,List<Location>> getLocations(Status status) {
        String requestURL = servicePath.concat(String.format("?%s=%s",
                LOCATION_STATUS_PARAM_NAME,status.name()));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,List<Location>> getLocations(Integer limit) {
        String requestURL = servicePath.concat(String.format("?%s=%d",
                LIMIT_PARAM_NAME,limit));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }


    public Either<ServiceError,Location> getLocation(Integer id) {
        String requestURL = servicePath.concat(String.format("/%d",id));
        return fetch(requestURL,HttpMethod.GET,Location.class);
    }

    public Either<ServiceError,Boolean> deleteLocation(Integer id) {
        String requestURL = servicePath.concat(String.format("/%d",id));
        return fetchNoResponseBody(requestURL,HttpMethod.DELETE);
    }

    public Either<ServiceError,Location> createLocation(LocationForm locationForm) {
        return fetchWithRequestBody(servicePath,HttpMethod.POST,Location.class,locationForm);
    }

    public Either<ServiceError,Location> patchLocation(Integer id, LocationPatch locationPatch) {
        String requestURL = servicePath.concat(String.format("/%d",id));
        return fetchWithRequestBody(requestURL,HttpMethod.PATCH,Location.class,locationPatch);
    }
}
