package org.voyager.commons.utils;

import org.junit.jupiter.api.Test;
import org.voyager.commons.constants.EnvVariableNames;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    @Test
    void getEnvVariable() {
        assertThrows(IllegalArgumentException.class,()->new Environment().validateEnvVars(List.of(
                EnvVariableNames.ADMIN_API_KEY
        )));
    }
}