package org.voyager.model.geoname;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
@Builder
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CountryGNResponse {
    @JsonProperty("geonames")
    List<CountryGN> countryGNList;
}
