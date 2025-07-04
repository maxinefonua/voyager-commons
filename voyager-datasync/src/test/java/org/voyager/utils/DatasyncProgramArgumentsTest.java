package org.voyager.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.model.Airline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.voyager.utils.DatasyncProgramArguments.*;

class DatasyncProgramArgumentsTest {
    private static final String HOST = "TEST_HOST";
    private static final String VALID_HOST_ARG = String.format("%s=%s",HOSTNAME_FLAG,HOST);
    private static final String ACCESS_TOKEN = "TEST_ACCESS";
    private static final String VALID_ACCESS_TOKEN_ARG = String.format("%s=%s", AUTH_TOKEN_FLAG,ACCESS_TOKEN);

    @Test
    @DisplayName("valid constructor")
    void constructor() {
        int expectedThreadCount = 45;
        String tcArg = String.format("%s=%d",THREAD_COUNT_FLAG,expectedThreadCount);

        String expectedHost = "localtest";
        String hArg = String.format("%s=%s",HOSTNAME_FLAG,expectedHost);

        int expectedPort = 1000;
        String pArg = String.format("%s=%d",PORT_FLAG,expectedPort);

        int expectedLimit = 5000;
        String lArg = String.format("%s=%d",LIMIT_FLAG,expectedLimit);

        String expectedToken = "accessToken";
        String atArg = String.format("%s=%s", AUTH_TOKEN_FLAG,expectedToken);

        Airline expectedAirline = Airline.DELTA;
        String aArg = String.format("%s=%s",AIRLINE_FLAG,"delta");

        DatasyncProgramArguments datasynced = new DatasyncProgramArguments(new String[]{
                tcArg,hArg,pArg,atArg,lArg,aArg});

        assertEquals(expectedThreadCount,datasynced.getThreadCount());
        assertEquals(expectedHost,datasynced.getHostname());
        assertEquals(expectedPort,datasynced.getPort());
        assertEquals(expectedToken,datasynced.getAccessToken());
        assertEquals(expectedLimit,datasynced.getProcessLimit());
        assertEquals(expectedAirline,datasynced.getAirline());
    }

    @Test
    @DisplayName("default constructor")
    void constructorDefault() {
        DatasyncProgramArguments datasynced = new DatasyncProgramArguments(new String[]{
                VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG
        });
        assertEquals(100,datasynced.getThreadCount());
        assertEquals(3000,datasynced.getPort());
        assertEquals(1000,datasynced.getProcessLimit());
        assertEquals(Airline.DELTA,datasynced.getAirline());
    }

    @Test
    @DisplayName("ignore non-flag argument")
    void ignoreNonFlag() {
        String nonFlagArg = "nonFlagArg";
        DatasyncProgramArguments datasynced = new DatasyncProgramArguments(new String[]{
                nonFlagArg,VALID_ACCESS_TOKEN_ARG,VALID_HOST_ARG});
    }

    @Test
    @DisplayName("missing access token on default")
    void constructorDefaultMissingAccess() {
        assertThrows(RuntimeException.class,() -> new DatasyncProgramArguments(new String[]{
                VALID_HOST_ARG
        }));
        try {
            new DatasyncProgramArguments(new String[]{VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(AUTH_TOKEN_FLAG));
        }
    }

    @Test
    @DisplayName("missing host on default")
    void constructorDefaultMissingHost() {
        assertThrows(RuntimeException.class,() -> new DatasyncProgramArguments(new String[]{
                VALID_ACCESS_TOKEN_ARG
        }));
        try {
            new DatasyncProgramArguments(new String[]{VALID_ACCESS_TOKEN_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(HOSTNAME_FLAG));
        }
    }

    @Test
    @DisplayName("unrecognized flag")
    void unrecognizedFlaggedArg() {
        String unrecognized = "-unknown=flag";
        assertThrows(RuntimeException.class,() -> new DatasyncProgramArguments(new String[]{unrecognized,VALID_HOST_ARG}));
        try {
            new DatasyncProgramArguments(new String[]{unrecognized,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Unrecognized flag"));
        }
    }

    @Test
    @DisplayName("invalid to default thread counts")
    void extractThreadCountInvalid() {
        int invalid = -20;
        int expected = 1;
        String tcArg = String.format("%s=%d",THREAD_COUNT_FLAG,invalid);
        DatasyncProgramArguments datasynced = new DatasyncProgramArguments(new String[]{
                tcArg,VALID_ACCESS_TOKEN_ARG,VALID_HOST_ARG});
        assertEquals(expected,datasynced.getThreadCount());

        invalid = 2000;
        expected = 1000;
        tcArg = String.format("%s=%d",THREAD_COUNT_FLAG,invalid);
        datasynced = new DatasyncProgramArguments(new String[]{
                tcArg,VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG});
        assertEquals(expected,datasynced.getThreadCount());
    }

    @Test
    @DisplayName("invalid to default process limit")
    void extractProcessLimitInvalid() {
        int invalid = 0;
        String tcArg = String.format("%s=%d",LIMIT_FLAG,invalid);
        DatasyncProgramArguments datasynced = new DatasyncProgramArguments(new String[]{
                tcArg,VALID_ACCESS_TOKEN_ARG,VALID_HOST_ARG});
        assertEquals(1,datasynced.getProcessLimit());
    }

    @Test
    @DisplayName("invalid input thread count")
    void extractThreadCountInvalidInput() {
        String invalidArg = String.format("%s=%s",THREAD_COUNT_FLAG,"invalid");
        assertThrows(RuntimeException.class,() -> new DatasyncProgramArguments(new String[]{invalidArg,VALID_HOST_ARG}));
        try {
            new DatasyncProgramArguments(new String[]{invalidArg,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(THREAD_COUNT_FLAG));
        }

        int valid = 10;
        String invalidArg2 = String.format("%s=%d=pl",THREAD_COUNT_FLAG,valid);
        assertThrows(RuntimeException.class,() -> new DatasyncProgramArguments(new String[]{invalidArg2,VALID_HOST_ARG}));
        try {
            new DatasyncProgramArguments(new String[]{invalidArg2,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(THREAD_COUNT_FLAG));
        }
    }

    @Test
    @DisplayName("invalid airline")
    void airlineInvalid() {
        String invalid = "invalid";
        String aArg = String.format("%s=%s",AIRLINE_FLAG,invalid);
        assertThrows(RuntimeException.class,() -> new DatasyncProgramArguments(new String[]{aArg,VALID_HOST_ARG}));
        try {
            new DatasyncProgramArguments(new String[]{aArg,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(AIRLINE_FLAG));
        }
    }

    @Test
    @DisplayName("invalid port")
    void portInvalid() {
        int invalid = -20;
        String pArg = String.format("%s=%d",PORT_FLAG,invalid);
        assertThrows(RuntimeException.class,() -> new DatasyncProgramArguments(new String[]{pArg,VALID_HOST_ARG}));
        try {
            new DatasyncProgramArguments(new String[]{pArg,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(PORT_FLAG));
        }

        invalid = 700000;
        String pArg2 = String.format("%s=%d",PORT_FLAG,invalid);
        assertThrows(RuntimeException.class,() -> new DatasyncProgramArguments(new String[]{pArg2,VALID_HOST_ARG}));
        try {
            new DatasyncProgramArguments(new String[]{pArg2,VALID_HOST_ARG});
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(PORT_FLAG));
        }
    }

    @Test
    @DisplayName("back to args")
    void convertBackToArgs() {
        DatasyncProgramArguments datasynced = new DatasyncProgramArguments(new String[]{
                VALID_HOST_ARG,VALID_ACCESS_TOKEN_ARG
        });
        String[] actual = datasynced.toArgs();
        assertTrue(Arrays.stream(actual).anyMatch(arg -> arg.equals(THREAD_COUNT_FLAG.concat("=").concat("100"))));
        datasynced.setThreadCountMax(1);

        Arrays.stream(actual).forEach(System.out::println);
    }
}