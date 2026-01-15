package org.voyager.commons.model.airport;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidCountryCode;
import java.util.List;
import java.util.StringJoiner;

@Data
@Builder
public class AirportQuery {
    @Min(1)
    @Max(1000)
    @Builder.Default
    private int size = 100;
    @Min(0)
    @Builder.Default
    private int page = 0;
    @ValidCountryCode(allowNull = true,caseSensitive = false)
    private String countryCode;
    private List<@NotNull Airline> airlineList;
    private List<@NotNull AirportType> airportTypeList;

    public String getRequestURL() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Path.AIRPORTS);
        urlBuilder.append("?");

        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s=%d",ParameterNames.PAGE,page));
        paramsJoiner.add(String.format("%s=%d",ParameterNames.SIZE,size));

        if (StringUtils.isNotBlank(countryCode)) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.COUNTRY_CODE_PARAM_NAME,countryCode.toUpperCase()));
        }
        if (airlineList != null && !airlineList.isEmpty()) {
            StringJoiner stringJoiner = new StringJoiner(",");
            airlineList.forEach(airline -> stringJoiner.add(airline.name()));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.AIRLINE_PARAM_NAME,stringJoiner));
        }
        if (airportTypeList != null) {
            StringJoiner typeJoiner = new StringJoiner(",");
            airportTypeList.forEach(airportType -> typeJoiner.add(airportType.name()));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.TYPE_PARAM_NAME,typeJoiner));
        }

        urlBuilder.append(paramsJoiner);
        return urlBuilder.toString();
    }
}
