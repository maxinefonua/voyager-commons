package org.voyager.model.language;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Language {
    String name;
    String iso6391;
    String iso6392;
    String iso6393;
}
