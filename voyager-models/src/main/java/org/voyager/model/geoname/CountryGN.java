package org.voyager.model.geoname;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class CountryGN {
    String continent;
    String capital;
    String languages;
    Long geonameId;
    Double south;
    String isoAlpha3;
    Double north;
    String fipsCode;
    String population;
    Double east;
    String isoNumeric;
    String areaInSqKm;
    String countryCode;
    Double west;
    String countryName;
    String postalCodeFormat;
    String continentName;
    String currencyCode;
}
