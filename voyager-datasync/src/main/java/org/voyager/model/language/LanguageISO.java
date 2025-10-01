package org.voyager.model.language;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor @Builder @AllArgsConstructor
@ToString(includeFieldNames = false)
public class LanguageISO {
    String alpha639code1;
    String alpha639code2;
    String alpha639code3;
    String name;
}
