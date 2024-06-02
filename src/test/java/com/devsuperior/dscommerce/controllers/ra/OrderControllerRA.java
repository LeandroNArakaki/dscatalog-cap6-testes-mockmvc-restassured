package com.devsuperior.dscommerce.controllers.ra;

import com.devsuperior.dscommerce.util.ra.TokenUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerRA {

    @LocalServerPort
    private int port;

    private Long existingOrderId, nonExistingOrderId;
    private String adminToken, clientToken, invalidToken;
    private String clientUsername, clientPassword, adminUsername, adminPassword;


    @BeforeEach
    void setUp() throws Exception {
        //baseURI = "http://localhost:8080";
        RestAssured.port = port;

        existingOrderId = 1L;
        nonExistingOrderId = 100L;

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        adminToken = TokenUtil.obtainAcessToken(adminUsername, adminPassword);
        clientToken = TokenUtil.obtainAcessToken(clientUsername, clientPassword);
        invalidToken = adminToken + "XPTO";


    }

    @Test
    void findByIdShouldReturnOrderWhenIdExistsAndAdminLogged() {
        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when().get("/orders/{id}", existingOrderId)
                .then().statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
                .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
                .body("total", is(1431.0F));
    }

    @Test
    void findByIdShouldReturnOrderWhenIdExistsAndClientLogged() {
        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when().get("/orders/{id}", existingOrderId)
                .then().statusCode(200)
                .body("id", is(1))
                .body("moment", equalTo("2022-07-25T13:00:00Z"))
                .body("status", equalTo("PAID"))
                .body("client.name", equalTo("Maria Brown"))
                .body("payment.moment", equalTo("2022-07-25T15:00:00Z"))
                .body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"))
                .body("total", is(1431.0F));
    }

    @Test
    void findByIdShouldReturnForbiddenWhenIdExistsAndClientLoggedAndOrderDoesNotBelongUser() {
        Long otherOrderId = 2L;

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when().get("/orders/{id}", otherOrderId)
                .then().statusCode(403);
    }

    @Test
    void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndAdminLogged() {
        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when().get("/orders/{id}", nonExistingOrderId)
                .then().statusCode(404);
    }

    @Test
    void findByIdShouldReturnNotFoundWhenIdDoesNotExistsAndClientLogged() {
        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when().get("/orders/{id}", nonExistingOrderId)
                .then().statusCode(404);
    }

    @Test
    void findByIdShouldReturnUnauthorizedWhenIdExistsAndInvalidToken() {
        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(ContentType.JSON)
                .when().get("/orders/{id}", nonExistingOrderId)
                .then().statusCode(401);
    }

}
