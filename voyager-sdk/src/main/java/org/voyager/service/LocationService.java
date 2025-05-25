package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.location.Location;
import org.voyager.model.location.LocationForm;

import java.util.List;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;

public class LocationService {
    private final String servicePath;

    LocationService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getLocationsPath();
    }

    public Either<ServiceError,List<Location>> getLocations() {
        return fetch(servicePath,HttpMethod.GET,new TypeReference<List<Location>>(){});
    }

    public Either<ServiceError,Location> getLocation(Integer id) {
        String requestURL = servicePath.concat(String.format("/%d",id));
        return fetch(requestURL,HttpMethod.GET,Location.class);
    }

    public Either<ServiceError,Location> createLocation(LocationForm locationForm) {
        return fetchWithRequestBody(servicePath,HttpMethod.POST,Location.class,locationForm);
    }
}
