package org.voyager.commons.model.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Route {
    Integer id;
    String origin;
    String destination;
    Double distanceKm;
}
