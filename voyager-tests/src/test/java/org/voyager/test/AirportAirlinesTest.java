package org.voyager.test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;
import org.junit.Test;
import org.voyager.config.VoyagerConfig;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.voyager.utils.ConstantsUtils.AUTH_TOKEN_HEADER_NAME;
import static org.voyager.utils.ConstantsUtils.IATA_PARAM_NAME;

public class AirportAirlinesTest {
    private static RequestSpecification requestSpec;
    @BeforeClass
    public static void setup() {
        String portConfig = VoyagerConfig.getProperty("voyager.port");
        if (portConfig != null && !portConfig.trim().isEmpty()) {
            RestAssured.port = Integer.parseInt(portConfig);
        }
        RestAssured.basePath = VoyagerConfig.getProperty("voyager.path.airport-airlines");
        String authToken = VoyagerConfig.getProperty("voyager.auth.token");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testGetAirlines() {
        given()
                .spec(requestSpec)
                .queryParam(IATA_PARAM_NAME, List.of("HEL"))
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("size()", greaterThan(0)) // Assert the response is a non-empty array
                .body("",hasItem("FINNAIR"));
    }
}
