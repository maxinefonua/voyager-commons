package org.voyager.model.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.voyager.model.validate.annotations.AllStringsMatchRegex;
import org.voyager.model.validate.annotations.ValidEnum;
import org.voyager.model.validate.annotations.ValidPatch;
import org.voyager.utils.Constants;

import java.util.List;

@Data @NoArgsConstructor
@Builder @ToString(includeFieldNames = false)
@AllArgsConstructor @ValidPatch
public class LocationPatch {
    @AllStringsMatchRegex(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3_CASE_SENSITIVE,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE_ELEMENTS_CASE_SENSITIVE)
    List<String> airports;

    @ValidEnum(enumClass = Status.class)
    String status;
}
