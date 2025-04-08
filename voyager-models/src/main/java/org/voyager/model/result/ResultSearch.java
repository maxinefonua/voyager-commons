package org.voyager.model.result;

import lombok.*;
import org.voyager.utils.MapperUtils;

@Builder @Getter @AllArgsConstructor
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class ResultSearch {
    String name;
    String adminName;
    String countryCode;
    String countryName;
    Double southBound;
    Double westBound;
    Double northBound;
    Double eastBound;
    Double longitude;
    Double latitude;
    String type;

    private static final MapperUtils<ResultSearch> mapper = new MapperUtils<>(ResultSearch.class);

    public String toJson() {
        return mapper.mapToJson(this);
    }

    public static ResultSearch fromJson(String jsonString) {
        return mapper.mapFromJson(jsonString);
    }
}
