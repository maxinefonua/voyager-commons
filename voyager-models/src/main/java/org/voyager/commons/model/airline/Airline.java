package org.voyager.commons.model.airline;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@SuppressWarnings("SpellCheckingInspection")
public enum Airline {
    DELTA("Delta Air Lines","dl-dal"),
    JAPAN("Japan Airlines","jl-jal"),
    NORWEGIAN("Norwegian","dy-noz"),
    SOUTHWEST("Southwest Airlines","wn-swa"),
    FINNAIR("Finnair","ay-fin"),
    AIRNZ("Air New Zealand","nz-anz"),
    HAWAIIAN("Hawaiian Airlines","ha-hal"),
    ALASKA("Alaska Airlines","as-asa"),
    UNITED("United Airlines","ua-ual"),
    ADVANCED("Advanced Air","an-wsn"),
    AEGEAN("Aegean Airlines","a3-aee"),
    AMERICAN("American Airlines","aa-aal"),
    FRONTIER("Frontier Airlines","f9-fft"),
    SPIRIT("Spirit Airlines","nk-nks"),
    VOLARIS("Volaris","y4-voi"),
    ZIPAIR("Zipair","zg-tzp"),
    AERLINGUS("Aer Lingus","ei-ein"),
    AEROMEXICO("Aeromexico","am-amx"),
    AIRCANADA("Air Canada","ac-aca"),
    AIRCHINA("Air China","ca-cca"),
    AIRFRANCE("Air France","af-afr"),
    ANA("All Nippon Airways","nh-ana"),
    ASIANA("Asiana Airlines","oz-aar"),
    EMIRATES("Emirates","ek-uae");

    private final String pathVariableFR;

    private final String displayText;

    Airline(String displayText, String pathVariableFR) {
        this.displayText = displayText;
        this.pathVariableFR = pathVariableFR;
    }

    public static Airline fromPathVariableFR(String pathVariableFR) {
        if (StringUtils.isBlank(pathVariableFR)) {
            throw new IllegalArgumentException("pathVariableFR must be an existing value");
        }
        if (pathVariableFR.equals("jl-jtl")) return Airline.JAPAN;
        for (Airline airline : values()) {
            if (pathVariableFR.equalsIgnoreCase(airline.pathVariableFR)) {
                return airline;
            }
        }
        throw new IllegalArgumentException("pathVariableFR must be an existing value");
    }

    public static Airline fromDisplayText(String displayText) {
        if (StringUtils.isBlank(displayText)) {
            throw new IllegalArgumentException("displayText must be an existing value");
        }
        for (Airline airline : values()) {
            if (displayText.equalsIgnoreCase(airline.displayText)) {
                return airline;
            }
        }
        throw new IllegalArgumentException("displayText must be an existing value");
    }
}
