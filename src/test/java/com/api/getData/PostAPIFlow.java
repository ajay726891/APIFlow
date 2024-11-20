package com.api.getData;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v122.network.Network;
import org.openqa.selenium.devtools.v122.network.model.RequestId;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

import java.util.Optional;

public class PostAPIFlow {

    private static String capturedRequestId;  // Variable to store the captured Request ID
    private static WebDriver driver; 
    private static boolean isRequestCaptured = false; // Flag to capture only the first Request ID

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit(); // Close the browser and end the WebDriver session
        }
    }

    @Test(priority = 1)
    public void getRequest() {
        // Make a GET request and capture the response
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/posts")
                .then()
                .extract().response();

        System.out.println("Response: " + response.asPrettyString());
        System.out.println("Status code: " + response.statusCode());

        // Assert the status code and validate the title
        Assert.assertEquals(200, response.statusCode());
        Assert.assertEquals("qui est esse", response.jsonPath().getString("title[1]"));
    }

    @Test(priority = 2)
    public void getNetworkValues() {
        final RequestId[] requestId = new RequestId[1];

        WebDriver driver = new ChromeDriver();
        DevTools devTools = ((HasDevTools) driver).getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        
			

        // Listen for network request events
        devTools.addListener(Network.requestWillBeSent(), request -> {
//        	System.out.println("Request URL: " + request.getRequest().getUrl());
//			System.out.println("Request Method: " + request.getRequest().getMethod());
//			System.out.println("Request ID: " + request.getRequestId());
//			System.out.println("---------------------------------------------------");
            if (!isRequestCaptured) { // Capture only the first request
                // Capture the Request ID from the network request
                requestId[0] = request.getRequestId();
                capturedRequestId = requestId[0].toString();  // Store the captured Request ID in a class variable
                System.out.println("Captured Request ID: " + capturedRequestId);
                isRequestCaptured = true;  // Set flag to true so it doesn't capture subsequent requests
            }
        });

        driver.get("https://jsonplaceholder.typicode.com");
    }

    @Test(priority = 3)
    public void postRequest() {
        // Ensure that the captured Request ID is available
        System.out.println("Captured Request ID for post: " + capturedRequestId);
        if (capturedRequestId == null) {
            Assert.fail("Request ID not captured from GET request.");
        }

        // Prepare the POST request payload
        String requestBody = "{"
                + "\"title\": \"Singh\","
                + "\"body\": \"Aditya\","
                + "\"userId\": 1"
                + "}";

        // Send the POST request with the captured Request ID as a custom header
        Response response = given()
                .contentType(ContentType.JSON)  
                .header("Request-Id", capturedRequestId)  
                .body(requestBody)  
                .when()
                .post("/posts") 
                .then()
                .extract().response(); 

        // Print response details
        System.out.println("Response: " + response.asPrettyString());
        System.out.println("Status code: " + response.statusCode());

        // Assertions to verify the POST request response
        Assert.assertEquals(201, response.statusCode());  
        Assert.assertEquals("Singh", response.jsonPath().getString("title"));  
        Assert.assertEquals("Aditya", response.jsonPath().getString("body"));   
        Assert.assertEquals(1, response.jsonPath().getInt("userId"));        
    }
}
