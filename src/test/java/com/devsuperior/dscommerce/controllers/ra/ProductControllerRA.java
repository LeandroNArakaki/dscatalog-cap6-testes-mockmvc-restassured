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

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerRA {

    @LocalServerPort
    private int port;

    private Long existingProductId, nonExistingProductId, dependentProductId;
    private String productName;
    private Map<String, Object> postProduct;
    private List<Map<String, Object>> categories;
    private String adminToken, clientToken, invalidToken;
    private String clientUsername, clientPassword, adminUsername, adminPassword;


    @BeforeEach
    void setUp() throws Exception {
        //baseURI = "http://localhost:8080";
        RestAssured.port = port;

        existingProductId = 25L;
        nonExistingProductId = 100L;
        dependentProductId = 3L;
        productName = "Macbook Pro";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        adminToken = TokenUtil.obtainAcessToken(adminUsername, adminPassword);
        clientToken = TokenUtil.obtainAcessToken(clientUsername, clientPassword);
        invalidToken = adminToken + "XPTO";

        postProduct = new HashMap<>();
        postProduct.put("name", "Meu Produto");
        postProduct.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProduct.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProduct.put("price", 50.0);

        categories = new ArrayList<>();
        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);

        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);

        categories.add(category1);
        categories.add(category2);
        postProduct.put("categories", categories);

    }

    @Test
    void findByIdShouldReturnProductWhenIdExists() {
        existingProductId = 2L;

        given().port(port).get("/products/{id}", existingProductId)
                .then()
                .statusCode(200)
                .body("id", is(2))
                .body("name", equalTo("Smart TV"))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
                .body("price", is(2190.0F))
                .body("categories.id", hasItems(2, 3))
                .body("categories.name", hasItems("Eletrônicos", "Computadores"));


    }

    @Test
    void findAllShouldReturnPageProductsWhenProductNameIsEmpty() {
        given().port(port)
                .get("/products?page=0")
                .then()
                .statusCode(200)
                .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));

    }

    @Test
    void findAllShouldReturnPageProductsWhenProductNameIsNotEmpty() {
        given().port(port)
                .get("/products?name={productName}", productName)
                .then()
                .statusCode(200)
                .body("content.id[0]", is(3))
                .body("content.name[0]", equalTo("Macbook Pro"))
                .body("content.price[0]", is(1250.0F))
                .body("content.imgUrl[0]", is("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
    }

    @Test
    void findAllShouldReturnPageProductWithPriceGreaterThan2000() {
        given().port(port)
                .get("/products?size=25")
                .then()
                .statusCode(200)
                .body("content.findAll {it.price > 2000}.name", hasItems("Smart TV", "PC Gamer Weed"));
    }

    @Test
    void insertShouldReturnProductCreatedWhenAdminLogged() {
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("Meu Produto"))
                .body("price", is(50.0F))
                .body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
                .body("categories.id", hasItems(2, 3));
    }

    @Test
    void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidName() {
        postProduct.put("name", "ab");
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Nome precisa ter de 3 a 80 caracteres"));
    }

    @Test
    void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidDescription() {
        postProduct.put("description", "ab");
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
    }

    @Test
    void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceIsNegative() {
        postProduct.put("price", -50.0);
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPriceIsZero() {
        postProduct.put("price", 0.0);
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    void insertShouldReturnUnprocessableEntityWhenProductHasNoCategory() {
        postProduct.put("categories", null);
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
    }

    @Test
    void insertShouldReturnForbiddenWhenClientLogged() {
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(403);
    }

    @Test
    void insertShouldReturnForbiddenWhenInvalidToken() {
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(401);
    }

    @Test
    void deleteShouldReturnNoContentWhenIdExistsAndAdminLogged() {
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/products/{id}", existingProductId)
                .then()
                .statusCode(204);
    }

    @Test
    void deleteShouldReturnNotFoundWhenIdDoesNotExistAndAdminLogged() {
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/products/{id}", nonExistingProductId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Recurso não encontrado"))
                .body("status", equalTo(404));
    }

    @Test
    void deleteShouldReturnBadRequestWhenDependentIdAndAdminLogged() {
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/products/{id}", dependentProductId)
                .then()
                .statusCode(400);
    }

    @Test
    void deleteShouldReturnForbiddenWhenClientLogged() {
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/products/{id}", existingProductId)
                .then()
                .statusCode(403);
    }

    @Test
    void deleteShouldReturnUnauthorizedWhenInvalidToken() {
        JSONObject newProduct = new JSONObject(postProduct);

        given()
                .port(port)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .delete("/products/{id}", existingProductId)
                .then()
                .statusCode(401);
    }





}
