package org.voyager.model.delta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Delta {
    String iata;
    DeltaStatus status;
    Boolean isHub;
}
