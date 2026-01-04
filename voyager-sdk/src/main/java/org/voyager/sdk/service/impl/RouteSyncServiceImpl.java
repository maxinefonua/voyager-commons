package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.route.RouteSync;
import org.voyager.commons.model.route.RouteSyncBatchUpdate;
import org.voyager.commons.model.route.RouteSyncPatch;
import org.voyager.commons.model.route.Status;
import org.voyager.commons.validate.ValidationUtils;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.service.RouteSyncService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;

import java.util.List;

public class RouteSyncServiceImpl implements RouteSyncService {
    private final ServiceUtils serviceUtils;

    RouteSyncServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    @SuppressWarnings("unused")
    RouteSyncServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    @Override
    public Either<ServiceError, List<RouteSync>> getByStatus(@NonNull Status status) {
        String requestURL = String.format("%s?%s=%s",Path.Admin.ROUTES.concat(Path.Admin.SYNC),
                ParameterNames.STATUS,status.name());
        return serviceUtils.fetch(requestURL, HttpMethod.GET, new TypeReference<>(){});
    }

    @Override
    public Either<ServiceError, Integer> batchUpdate(@NonNull RouteSyncBatchUpdate routeSyncBatchUpdate) {
        ValidationUtils.validateAndThrow(routeSyncBatchUpdate);
        String requestURL = Path.Admin.ROUTES.concat(Path.Admin.SYNC);
        return serviceUtils.fetchWithRequestBody(requestURL, HttpMethod.PATCH, Integer.class,routeSyncBatchUpdate);
    }

    @Override
    public Either<ServiceError, RouteSync> patchRouteSync(@NonNull Integer routeId,
                                                          @NonNull RouteSyncPatch routeSyncPatch) {
        ValidationUtils.validateAndThrow(routeSyncPatch);
        String requestURL = String.format("%s/%d",Path.Admin.ROUTES.concat(Path.Admin.SYNC),routeId);
        return serviceUtils.fetchWithRequestBody(requestURL, HttpMethod.PATCH, RouteSync.class,routeSyncPatch);
    }
}
