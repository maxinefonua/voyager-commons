package org.voyager.sdk.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.location.Location;
import org.voyager.commons.model.location.LocationForm;
import org.voyager.commons.model.location.LocationPatch;
import org.voyager.commons.model.location.Source;
import org.voyager.sdk.model.LocationQuery;
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
