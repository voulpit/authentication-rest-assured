package com.hanul.pis.authentication.restassured;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class UsersEndpointTest {
    private static final String CONTEXT_PATH = "/authentication";
    private static final String JSON = "application/json";

    private static String authToken; // if not static, it won't retain the value from one test to another
    private static String userId;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8088;

        // Must execute the following queries for the tests to succeed:
        // update users set active_ind=1 where id=3;
        // update users_roles set roles_id=2 where users_id=3; -- setting admin role
        // at http://localhost:8088/authentication/h2-console/
    }

    @Test
    final void test_1_UserLogin() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", "mimirose1052016@yahoo.com");
        credentials.put("password", "clopotel");

        // dependent on email verification
        Response response = RestAssured.given()
                .contentType(JSON).accept(JSON).body(credentials)
                .when().post(CONTEXT_PATH + "/get-me-in")
                .then().statusCode(200)
                .extract().response();

        authToken = response.header("Authorization");
        Assertions.assertNotNull(authToken);

        userId = response.header("UserId");
        Assertions.assertNotNull(userId);
    }

    @Test
    final void test_2_GetUserDetails() {
        Response response = RestAssured.given()
                .header("Authorization", authToken).contentType(JSON).accept(JSON)
                .when().get(CONTEXT_PATH + "/users/" + userId)
                .then().statusCode(200)
                .extract().response();

        String userPublicId = response.jsonPath().getString("userId");
        Assertions.assertEquals(userId, userPublicId);

        String email = response.jsonPath().getString("email");
        Assertions.assertNotNull(email);

        List<Map<String, String>> addresses = response.jsonPath().getList("addresses");
        Assertions.assertNull(addresses);
    }

    @Test
    final void test_3_UpdateUserDetails() {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("firstName", "Meem");
        userInfo.put("lastName", "Rose");

        Response response = RestAssured.given()
                .header("Authorization", authToken).contentType(JSON).accept(JSON).body(userInfo)
                .when().put(CONTEXT_PATH + "/users/" + userId)
                .then().statusCode(200)
                .extract().response();

        String firstName = response.jsonPath().getString("firstName");
        Assertions.assertNotNull(firstName);
        Assertions.assertEquals("Meem", firstName);

        String lastName = response.jsonPath().getString("lastName");
        Assertions.assertNotNull(lastName);
        Assertions.assertEquals("Rose", lastName);
    }

    @Test
    final void test_4_DeleteUser() {
        // dependent on user having ADMIN role
        Response response = RestAssured.given()
                .header("Authorization", authToken).contentType(JSON).accept(JSON)
                .when().delete(CONTEXT_PATH + "/users/" + userId)
                .then().statusCode(200)
                .extract().response();

        boolean success = response.jsonPath().getBoolean("successful");
        Assertions.assertTrue(success);
    }
}
