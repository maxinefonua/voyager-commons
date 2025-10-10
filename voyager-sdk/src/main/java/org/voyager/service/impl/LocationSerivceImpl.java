package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.LocationQuery;
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.Source;
import org.voyager.model.location.Location;
import org.voyager.model.location.LocationPatch;
import org.voyager.service.LocationService;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;
import java.util.List;

public class LocationSerivceImpl implements LocationService {
    private final ServiceUtils serviceUtils;

    LocationSerivceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    LocationSerivceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }


    @Override
    public Either<ServiceError, List<Location>> getLocations() {
        String requestURL = Constants.Voyager.Path.LOCATIONS;
        return serviceUtils.fetch(requestURL, HttpMethod.GET, new TypeReference<List<Location>>() {});
    }

    @Override
    public Either<ServiceError, List<Location>> getLocations(LocationQuery locationQuery) {
        return serviceUtils.fetch(locationQuery.getRequestURL(), HttpMethod.GET, new TypeReference<List<Location>>() {});
    }

    @Override
    public Either<ServiceError, Location> getLocation(@NonNull Source source, @NonNull String sourceId) {
        String requestURL = String.format("%s" + "?%s=%s" + "&%s=%s", Constants.Voyager.Path.LOCATION,
                Constants.Voyager.ParameterNames.SOURCE_PARAM_NAME,source.name(),
                Constants.Voyager.ParameterNames.SOURCE_ID_PARAM_NAME,sourceId);
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Location.class);
    }

    @Override
    public Either<ServiceError, Location> getLocation(@NonNull Integer id) {
        String requestURL = Constants.Voyager.Path.LOCATIONS.concat(String.format("/%d",id));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Location.class);
    }

    @Override
    public Either<ServiceError, Void> deleteLocation(@NonNull Integer id) {
        String requestURL = Constants.Voyager.Path.LOCATIONS.concat(String.format("/%d",id));
        return serviceUtils.fetchNoResponseBody(requestURL,HttpMethod.DELETE);
    }

    @Override
    public Either<ServiceError, Location> createLocation(@NonNull @Valid LocationForm locationForm) {
        return serviceUtils.fetchWithRequestBody(Constants.Voyager.Path.LOCATIONS,HttpMethod.POST,Location.class,locationForm);
    }

    @Override
    public Either<ServiceError, Location> patchLocation(@NonNull Integer id, @NonNull @Valid LocationPatch locationPatch) {
        String requestURL = Constants.Voyager.Path.LOCATIONS.concat(String.format("/%d",id));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Location.class,locationPatch);
    }
}
