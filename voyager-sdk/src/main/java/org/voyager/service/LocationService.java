package org.voyager.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.model.LocationQuery;
import org.voyager.model.location.*;
import java.util.List;

public interface LocationService {
    Either<ServiceError,List<Location>> getLocations();
    Either<ServiceError,List<Location>> getLocations(LocationQuery locationQuery);
    Either<ServiceError,Location> getLocation(@NonNull Source source, @NonNull String sourceId);
    Either<ServiceError,Location> getLocation(@NonNull Integer id);
    Either<ServiceError,Void> deleteLocation(@NonNull Integer id);
    Either<ServiceError,Location> createLocation(@NonNull @Valid LocationForm locationForm);
    Either<ServiceError,Location> patchLocation(@NonNull Integer id, @NonNull @Valid LocationPatch locationPatch);
}
