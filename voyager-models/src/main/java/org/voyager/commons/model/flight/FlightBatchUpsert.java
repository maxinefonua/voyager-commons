package org.voyager.commons.model.flight;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @NoArgsConstructor
@Builder @AllArgsConstructor
public class FlightBatchUpsert {
    @NotEmpty
    List<@Valid FlightUpsert> flightUpsertList;
}
