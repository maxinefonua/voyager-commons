package org.voyager.model.airline;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.voyager.model.validate.annotations.AllStringsMatchRegex;
import org.voyager.model.validate.annotations.ValidEnum;
import org.voyager.utils.Constants;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(includeFieldNames = false)
public class AirlineBatchUpsert {
    @NotBlank
    @ValidEnum(enumClass = Airline.class)
    String airline;

    @NotEmpty
    @AllStringsMatchRegex(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE_ELEMENTS)
    List<String> iataList;

    @NotNull
    Boolean isActive;
}
