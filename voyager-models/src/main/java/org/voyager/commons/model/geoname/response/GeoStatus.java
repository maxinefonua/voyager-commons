package org.voyager.commons.model.geoname.response;

import lombok.*;

@Builder
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
@ToString
public class GeoStatus {
    String message;
    Integer value;
}
