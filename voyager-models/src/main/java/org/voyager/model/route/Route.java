package org.voyager.model.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.Airline;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Route {
    Integer id;
    String origin;
    String destination;
    List<Integer> flightIds;
}
