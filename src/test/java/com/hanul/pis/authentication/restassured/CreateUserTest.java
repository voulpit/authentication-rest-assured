package com.hanul.pis.authentication.restassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Fail.fail;

/*
    Integration test using RestAssured
    The API under test must be running an in-memory database so that test data would not pollute the real DB (spring.profiles.active=test)
 */
public class CreateUserTest {
    private static final String CONTEXT_PATH = "/authentication";

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8088;
    }

    @Test
    final void testCreateUser() {
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("firstName", "Mimi");
        userDetails.put("lastName", "Rozz");
        userDetails.put("email", "mimirose1052016@yahoo.com");
        userDetails.put("password", "clopotel");

        Response response = RestAssured.given()
                .contentType("application/json").accept("application/json").body(userDetails)
                .when().post(CONTEXT_PATH + "/users")
                .then().statusCode(200).contentType("application/json")
                .extract().response();

        String userId = response.jsonPath().getString("userId");
        Assertions.assertNotNull(userId);
        Assertions.assertEquals(30, userId.length());

        String bodyString = response.body().asString();
        try {
            JSONObject responseBodyJson = new JSONObject(bodyString);
            String email = responseBodyJson.getString("email");
            Assertions.assertNotNull(email);
            Assertions.assertEquals("mimirose1052016@yahoo.com", email);

        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }
}
