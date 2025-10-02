package org.voyager.model.geoname;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Builder @Data
@ToString @NoArgsConstructor
@AllArgsConstructor
public class CountryGNResponse {
    @JsonProperty("geonames")
    private List<CountryGN> countryGNList;
}
