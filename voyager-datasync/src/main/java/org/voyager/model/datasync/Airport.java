package org.voyager.model.datasync;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(includeFieldNames = false)
public class Airport {
    @ToString.Exclude
    String country;
    String iata;
    @ToString.Exclude
    String icao;
    @ToString.Exclude
    Double lat;
    @ToString.Exclude
    Double lon;
    @ToString.Exclude
    String name;
}
