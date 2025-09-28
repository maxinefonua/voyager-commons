package org.voyager.test;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;
import org.voyager.config.VoyagerConfig;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.voyager.utils.ConstantsUtils.AUTH_TOKEN_HEADER_NAME;

public class HealthTest {
    private String authToken;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        String portConfig = VoyagerConfig.getProperty("voyager.port");
        if (portConfig != null && !portConfig.trim().isEmpty()) {
            RestAssured.port = Integer.parseInt(portConfig);
        }
        RestAssured.basePath = VoyagerConfig.getProperty("voyager.path.health");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        authToken = VoyagerConfig.getProperty("voyager.auth.token");
        requestSpec = new RequestSpecBuilder()
                .addHeader("Accept", "application/json")
                .addHeader(AUTH_TOKEN_HEADER_NAME, authToken)
                .build();
    }

    @Test
    public void testAPIHealth() {
        given()
                .spec(requestSpec)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }
}
