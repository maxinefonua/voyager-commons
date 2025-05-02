package org.voyager.model.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LocationDisplay {
    private Integer id;
    private String name;
    private String subdivision;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private Double[] bbox;
    private Status status;
    private Source source;
    private String sourceId;
}