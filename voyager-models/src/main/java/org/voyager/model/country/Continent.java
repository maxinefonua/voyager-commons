package org.voyager.model.country;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Continent {
    OC("Oceania"),
    AS("Asia"),
    SA("South America"),
    AF("Africa"),
    AN("Antarctica"),
    NA("North America"),
    EU("Europe");

    private String displayText;
    Continent(String displayText) {
        this.displayText = displayText;
    }

    public static Continent fromDisplayText(String displayText) {
        return Arrays.stream(values())
                .filter(continent -> continent.displayText.equalsIgnoreCase(displayText))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No continent found for: " + displayText
                ));
    }

    @JsonValue
    public String getDisplayText() {
        return this.displayText;
    }
}
