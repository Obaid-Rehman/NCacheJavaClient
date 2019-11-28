package com.example.ncachejavaclient;

import java.util.List;
import java.util.StringTokenizer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.alachisoft.ncache.web.caching.CacheInitParams;
import com.alachisoft.ncache.web.caching.CacheMode;
import com.alachisoft.ncache.web.caching.CacheServerInfo;
import com.alachisoft.ncache.web.caching.ClientCacheSyncMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class NcachejavaclientApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(NcachejavaclientApplication.class);
	
	@Autowired
	private Environment env;
	
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
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
	
	@Bean
	public CacheInfo cacheInfo()
	{
		CacheInitParams connectionParams = null;
		try { 
			connectionParams = this.Invoke(); 
		} 
		catch (JsonProcessingException e) { 
			// TODO Auto-generated catch block 
			LOGGER.error("Error occurred in " + e.toString());
			 e.printStackTrace(); } catch (Exception e) { LOGGER.error("Error occurred in "
			  + e.toString()); e.printStackTrace(); }
		
		return new CacheInfo(env.getProperty("CacheID"), connectionParams);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		LOGGER.info("{}", env.getProperty("CacheID"));
		LOGGER.info("{}", env.getProperty("NCacheDiscoveryURL"));
	}
	
	private CacheInitParams Invoke() throws JsonMappingException, JsonProcessingException {
		
		
		RestTemplate restTemplate = restTemplateBuilder.build(); 
		String ncacheDiscoveryUrl = env.getProperty("NCacheDiscoveryURL"); 
		
		ResponseEntity<String> response = restTemplate.getForEntity(ncacheDiscoveryUrl, String.class);
		String stringData = response.getBody();
		
		LOGGER.info("Json string data received:"+stringData);
		
		ObjectMapper objectMapper = new ObjectMapper(); 
		TypeReference<HashMap<String,ArrayList<String>>> typeRef= new TypeReference<HashMap<String,ArrayList<String>>>() { }; 
		HashMap<String, ArrayList<String>> map = objectMapper.readValue(stringData, typeRef);
		
		
		ArrayList<String> serverList = map.get("cache-client");
		
		LOGGER.info("Total number of NCache servers:"+serverList.size());
		
		ArrayList<CacheServerInfo> cacheServers = new ArrayList<CacheServerInfo>();
		String temp = "";
		StringTokenizer tokenizer = null;
		String ipAddress = "";
		int port = -1;
		
		
		for (String server:serverList) {
			temp = server.replace("tcp://", "").replace("/", "");
			tokenizer = new StringTokenizer(temp,":",false);
			ipAddress = tokenizer.nextToken();
			port = Integer.parseInt(tokenizer.nextToken());
			
			try {
				CacheServerInfo cacheServer = new CacheServerInfo();
				cacheServer.setName(ipAddress);
				cacheServer.setPort(port);
				cacheServers.add(cacheServer);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				LOGGER.error("UnknownHostException "+e.getMessage());
				e.printStackTrace();
			}
			
		}

		CacheServerInfo[] ncacheServers = new CacheServerInfo[cacheServers.size()];
		
		for (int i = 0; i < ncacheServers.length; i++)
		{
			ncacheServers[i] = cacheServers.get(i);
		}
		
		CacheInitParams connectionParams = new CacheInitParams();

		connectionParams.setClientCacheSyncMode(ClientCacheSyncMode.Optimistic);
		connectionParams.setBindIP("");
		connectionParams.setClientRequestTimeOut(30);
		connectionParams.setConnectionRetries(3);
		connectionParams.setConnectionTimeout(5);
		connectionParams.setDefaultReadThruProvider("");
		connectionParams.setDefaultWriteThruProvider("");
		connectionParams.setEnableClientLogs(true);
		connectionParams.setEnableDetailedClientLogs(true);
		connectionParams.setLoadBalance(true);
		connectionParams.setMode(CacheMode.OutProc);
		connectionParams.setRetryConnectionDelay(5);
		connectionParams.setRetryInterval(3);
		connectionParams.setServerList(ncacheServers);
		
		
		return connectionParams;
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
