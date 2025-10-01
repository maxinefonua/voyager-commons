package org.voyager.model.language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import static org.voyager.utils.ConstantsUtils.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(includeFieldNames = false)
public class LanguageForm {
    @NotBlank
    String name;
    @Pattern(regexp = ALPHA2_CODE_REGEX_OR_EMPTY, message = LANGUAGE_ISO639_1_CONSTRAINT)
    String iso6391;

    @Pattern(regexp = ALPHA3_CODE_REGEX_OR_EMPTY, message = LANGUAGE_ISO639_2_CONSTRAINT)
    String iso6392;

    @Pattern(regexp = ALPHA3_CODE_REGEX_OR_EMPTY, message = LANGUAGE_ISO639_3_CONSTRAINT)
    String iso6393;
}
