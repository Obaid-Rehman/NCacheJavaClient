package com.example.ncachejavaclient;

import java.util.List;
import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class NcachejavaclientApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(NcachejavaclientApplication.class, args);
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any())
				.build();
	}
	
	
	private ApiInfo apiInfo()
	{
		Contact contact = new Contact("Brad Rehman", "http://www.alachisoft.com", "obaid@alachisoft.com");
		
		List<VendorExtension> vendorExtensions = new ArrayList<VendorExtension>();
		
		return new ApiInfo("NCache Java Client API", 
				"CRUD ops on NCache from Java application", 
				"V1", 
				"Alachisoft TOS", 
				contact, 
				"Apache 2.0", 
				"http://www.apache.org/licenses/LICENSE-2.0", 
				vendorExtensions);
	}
}
