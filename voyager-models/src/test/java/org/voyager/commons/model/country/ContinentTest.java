package org.voyager.commons.model.country;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.country.Continent;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContinentTest {
    private static Continent continent;
    private static String DISPLAY_TEXT_OC = "Oceania";
    private static String DISPLAY_TEXT_AS = "Asia";

    @BeforeEach
    void setUp() {
        continent = Continent.OC;
    }

    @Test
    @DisplayName("test constructor")
    void testConstructor() {
        assertNotNull(continent);
    }

    @Test
    void fromDisplayText() {
        assertEquals(continent,Continent.fromDisplayText(DISPLAY_TEXT_OC));
    }

    @Test
    void getDisplayText() {
        continent = Continent.AS;
        assertEquals(DISPLAY_TEXT_AS,continent.getDisplayText());
    }
}