package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.model.LocationQuery;
import org.voyager.commons.model.location.LocationForm;
import org.voyager.commons.model.location.Source;
import org.voyager.commons.model.location.Location;
import org.voyager.commons.model.location.LocationPatch;
import org.voyager.sdk.service.LocationService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;
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
        String requestURL = Path.Admin.LOCATIONS;
        return serviceUtils.fetch(requestURL, HttpMethod.GET, new TypeReference<List<Location>>() {});
    }

    @Override
    public Either<ServiceError, List<Location>> getLocations(LocationQuery locationQuery) {
        return serviceUtils.fetch(locationQuery.getRequestURL(), HttpMethod.GET, new TypeReference<List<Location>>() {});
    }

    @Override
    public Either<ServiceError, Location> getLocation(@NonNull Source source, @NonNull String sourceId) {
        String requestURL = String.format("%s" + "?%s=%s" + "&%s=%s", Path.Admin.LOCATION,
                ParameterNames.SOURCE_PARAM_NAME,source.name(),
                ParameterNames.SOURCE_ID_PARAM_NAME,sourceId);
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Location.class);
    }

    @Override
    public Either<ServiceError, Location> getLocation(@NonNull Integer id) {
        String requestURL = Path.Admin.LOCATIONS.concat(String.format("/%d",id));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Location.class);
    }

    @Override
    public Either<ServiceError, Void> deleteLocation(@NonNull Integer id) {
        String requestURL = Path.Admin.LOCATIONS.concat(String.format("/%d",id));
        return serviceUtils.fetchNoResponseBody(requestURL,HttpMethod.DELETE);
    }

    @Override
    public Either<ServiceError, Location> createLocation(@NonNull @Valid LocationForm locationForm) {
        return serviceUtils.fetchWithRequestBody(Path.Admin.LOCATIONS,HttpMethod.POST,Location.class,locationForm);
    }

    @Override
    public Either<ServiceError, Location> patchLocation(@NonNull Integer id, @NonNull @Valid LocationPatch locationPatch) {
        String requestURL = Path.Admin.LOCATIONS.concat(String.format("/%d",id));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Location.class,locationPatch);
    }
}
