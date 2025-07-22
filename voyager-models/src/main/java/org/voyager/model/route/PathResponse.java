package org.voyager.model.route;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.Airline;

import java.util.List;

@Builder
@Data @NoArgsConstructor
@AllArgsConstructor
public class PathResponse<T> {
    @Builder.Default
    Integer count = 0;
    @Builder.Default
    List<Airline> airlines = List.of();
    @Builder.Default
    List<T> responseList = List.of();
}
