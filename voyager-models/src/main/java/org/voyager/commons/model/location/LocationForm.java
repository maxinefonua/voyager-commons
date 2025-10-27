package org.voyager.commons.model.location;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.commons.validate.annotations.ValidCountryCode;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidLatitude;
import org.voyager.commons.validate.annotations.ValidLongitude;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class LocationForm {

    @NotNull
    @ValidEnum(enumClass = Source.class)
    @Builder.Default
    String source = Source.MANUAL.name();

    @NotBlank
    String sourceId;

    @NotBlank
    String name;

    @NotBlank
    String subdivision;

    @ValidCountryCode
    String countryCode;

    @ValidLatitude
    Double latitude;

    @ValidLongitude
    Double longitude;

    @ValidLongitude
    Double west;

    @ValidLatitude
    Double south;

    @ValidLongitude
    Double east;

    @ValidLatitude
    Double north;

    @NotNull
    @Builder.Default
    List<String> airports = new ArrayList<>();
}
