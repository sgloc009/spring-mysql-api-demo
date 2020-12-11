package com.example.demo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.json.*;

import com.example.sql.*;

@SpringBootApplication
@RestController
public class DemoApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		SqlDatabase.createTables();
	}
	
	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
	return String.format("Hello %s!", name);
	}

	@PostMapping(value="/order/create", produces=MediaType.APPLICATION_JSON_VALUE)
	public String createOrders(@RequestBody() String body){
		System.out.println(body.length());
		JSONArray jbody = new JSONArray(body);
		System.out.println(jbody.length());
		return SqlDatabase.createOrders(jbody);
	}

	@GetMapping(value = "/orders", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getAllOrders(){
		return SqlDatabase.getAllOrders();
	}

	@GetMapping(value = "/orders/{orderid}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getSpecificOrder(@PathVariable("orderid") int orderid){
		return SqlDatabase.getOrder(orderid);
	}

}
