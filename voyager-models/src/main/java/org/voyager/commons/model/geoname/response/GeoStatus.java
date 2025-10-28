package org.voyager.commons.model.geoname.response;

import lombok.Setter;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
