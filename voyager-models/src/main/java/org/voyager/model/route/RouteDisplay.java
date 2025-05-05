package org.voyager.model.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.Airline;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RouteDisplay {
    Integer id;
    String origin;
    String destination;
    Airline airline;
}
