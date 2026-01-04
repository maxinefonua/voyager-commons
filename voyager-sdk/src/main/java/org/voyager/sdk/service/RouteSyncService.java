package org.voyager.sdk.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.route.RouteSync;
import org.voyager.commons.model.route.RouteSyncBatchUpdate;
import org.voyager.commons.model.route.RouteSyncPatch;
import org.voyager.commons.model.route.Status;
import java.util.List;

public interface RouteSyncService {
    Either<ServiceError,List<RouteSync>> getByStatus(@NonNull Status status);
    Either<ServiceError,Integer> batchUpdate(@NonNull RouteSyncBatchUpdate routeSyncBatchUpdate);
    Either<ServiceError,RouteSync> patchRouteSync(@NonNull Integer routeId,@NonNull  RouteSyncPatch routeSyncPatch);
}
