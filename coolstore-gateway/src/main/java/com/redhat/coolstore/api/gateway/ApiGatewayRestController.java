package com.redhat.coolstore.api.gateway;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class ApiGatewayRestController {

	@Value("${catalog.service.url}")
	private String catalogServiceUrl;
	
	private String catalogServiceProductsEndpoint;
	
	private RestTemplate restTemplate;
	
	public ApiGatewayRestController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@PostConstruct
	private void setup() {
		catalogServiceProductsEndpoint = catalogServiceUrl + "/products";
	}
	
	@GetMapping("/products")
	public String getProducts() {
		
		String result = restTemplate.getForObject(catalogServiceProductsEndpoint, String.class);
		
		return result;		
	}
}
