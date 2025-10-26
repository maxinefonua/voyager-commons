package org.voyager.constants;

import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.Regex;

import static org.junit.jupiter.api.Assertions.*;

class RegexTest {

    @Test
    void testRegex() {
        assertNotNull(Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_NONEMPTY);
        assertNotNull(Regex.AIRPORT_CODE);
    }
}