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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
 
import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
 
public class APIFlow {
 
	@BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }
 
	@Test(priority = 1)
    public void getRequest() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/posts")
                .then()
                .extract().response();
        System.out.println("Response: "+response.asPrettyString());
        System.out.println("Status code: "+response.statusCode());
 
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
		
		
		devTools.addListener(Network.requestWillBeSent(), request -> {
			System.out.println("Request URL: " + request.getRequest().getUrl());
			System.out.println("Request Method: " + request.getRequest().getMethod());
			System.out.println("Request ID: " + request.getRequestId());
			System.out.println("Request Type: " + request.getType());
			
			requestId[0]=request.getRequestId();
			System.out.println("This is RequestID:"+requestId[0]);
			
			String requestUrl = request.getRequest().getUrl();
			System.out.println("This is requestURL:"+requestUrl);
			
			String requestMethod = request.getRequest().getMethod();
			System.out.println("This is requestMethod:"+requestMethod);
			
			
			
			System.out.println("**********************************************");
		});
		devTools.addListener(Network.responseReceived(), response -> {
			System.out.println("Response URL: " + response.getResponse().getUrl());
			System.out.println("Response Status: " + response.getResponse().getStatus());
			System.out.println("Response Status Text: " + response.getResponse().getStatusText());
			System.out.println("Response ID: " + response.getRequestId());
			System.out.println("Response Type: " + response.getType());
			System.out.println("**********************************************");
		});
		devTools.addListener(Network.dataReceived(), data -> {
			System.out.println("Data ID: " + data.getRequestId());
			System.out.println("Data Received: " + data.getDataLength());
			System.out.println("**********************************************");
		});
		devTools.addListener(Network.loadingFinished(), load -> {
			System.out.println("Load ID: " + load.getRequestId());
			System.out.println("Load Finished");
			System.out.println("**********************************************");
		});
		driver.get("https://jsonplaceholder.typicode.com");
    }
}