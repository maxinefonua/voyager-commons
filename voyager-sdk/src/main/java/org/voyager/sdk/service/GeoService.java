package org.voyager.sdk.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.geoname.GeoCountry;
import org.voyager.commons.model.geoname.GeoFull;
import org.voyager.commons.model.geoname.GeoPlace;
import org.voyager.commons.model.geoname.GeoTimezone;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;

import java.util.List;

public interface GeoService {
    Either<ServiceError,List<GeoPlace>> findNearbyPlaces(@NonNull GeoNearbyQuery geoNearbyQuery);
    Either<ServiceError,GeoTimezone> getTimezone(@NonNull GeoTimezoneQuery geoTimezoneQuery);
    Either<ServiceError,List<GeoPlace>> search(@NonNull GeoSearchQuery geoSearchQuery);
    Either<ServiceError,GeoFull> getFull(@NonNull Long geoNameId);
    Either<ServiceError,List<GeoCountry>> getCountries();
}
