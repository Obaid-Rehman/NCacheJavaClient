package com.example.ncachejavaclient;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alachisoft.ncache.runtime.caching.Tag;
import com.alachisoft.ncache.runtime.exceptions.CacheException;
import com.alachisoft.ncache.runtime.exceptions.ConfigurationException;
import com.alachisoft.ncache.runtime.exceptions.GeneralFailureException;
import com.alachisoft.ncache.web.caching.Cache;
import com.alachisoft.ncache.web.caching.CacheInitParams;
import com.alachisoft.ncache.web.caching.CacheItem;
import com.alachisoft.ncache.web.caching.CacheMode;
import com.alachisoft.ncache.web.caching.CacheServerInfo;
import com.alachisoft.ncache.web.caching.ClientCacheSyncMode;
import com.alachisoft.ncache.web.caching.NCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class NCacheController {
	
	private Environment env;
	private RestTemplateBuilder restTemplateBuilder;
	
	private Cache cache;
	private static final Tag tag = new Tag("items");
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NCacheController.class);
	
	@Autowired
	public NCacheController(Environment environment, RestTemplateBuilder restTemplateBuilder) {
		this.restTemplateBuilder = restTemplateBuilder;
		this.env = environment;
	}
	
	@RequestMapping(value="/api/ncache", method = RequestMethod.GET)
	public HashMap<String, String> getAll(){
		InitializeCache();
		HashMap<String,String> result = new HashMap<String,String>();
		LOGGER.info("Getting all cached values");
		
		try {
			HashMap map = cache.getByTag(this.tag);
			if (!map.isEmpty())
			{
				Iterator iter = map.values().iterator();
				Iterator iter2 = map.keySet().iterator();
				while (iter.hasNext())
				{
					result.put(iter2.next().toString(), iter.next().toString());
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception "+e.getMessage());
			e.printStackTrace();
		}
		
		return result;
	}
	
	@RequestMapping(value="/api/ncache/{key}", method = RequestMethod.GET)
	public String getByKey(@PathVariable("key") String key)
	{
		InitializeCache();
		LOGGER.info("Getting value of key " + key);
		
		String value = null;
		try {
			value = cache.get(key).toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception "+e.getMessage());
			e.printStackTrace();
		}
		
		return value;
	}
	
	@RequestMapping(value="/api/ncache/{key}", method = RequestMethod.POST)
	public String add(@PathVariable("key") String key, @RequestBody String value)
	{
		InitializeCache();
		LOGGER.info("Adding value "+ value + " against key " + key);
		try {
			if (cache.contains(key) || value == null)
			{
				return null;
			}
			else
			{
				CacheItem item = new CacheItem(value);
				item.setTags(new Tag[] {tag});
				cache.add(key, item);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception "+e.getMessage());
			e.printStackTrace();
		}
		
		return "Value added " + value;
	}
	
	@RequestMapping(value="/api/ncache/{key}", method = RequestMethod.PUT)
	public String update(@PathVariable("key") String key, @RequestBody String value)
	{
		InitializeCache();
		LOGGER.info("Upserting value "+ value + " against key " + key);
		try {
				CacheItem item = new CacheItem(value);
				item.setTags(new Tag[] {tag});
				cache.insert(key, item);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception "+e.getMessage());
			e.printStackTrace();
		}
		
		return "Value Updated " + value + " against key "+ key;
	}
	
	@RequestMapping(value="/api/ncache/{key}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("key") String key)
	{
		InitializeCache();
		String value = null;
		
		LOGGER.info("Deleting key " + key);
		try {
				value = cache.remove(key).toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception "+e.getMessage());
			e.printStackTrace();
		}
		
		return value;
	}
	
	private void InitializeCache()
	{
		if (cache == null){
				try {
					CacheInitParams connParams = this.getCacheConnectionParams();
					String cacheID = this.env.getProperty("CacheID");
					
					LOGGER.info("Cache ID:"+cacheID);
					LOGGER.info("Cache Servers Available:");
					
					CacheServerInfo[] cacheServers = connParams.getServerList();
					
					int i = 1;
					for (CacheServerInfo server: cacheServers)
					{
						LOGGER.info("Server "+i+": "+ server.getName() + " "+ server.getPort());
					}
					
					cache = NCache.initializeCache(cacheID, connParams);
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					LOGGER.error("ConfigurationException "+e.getMessage());
					e.printStackTrace();
				} catch (GeneralFailureException e) {
					// TODO Auto-generated catch block
					LOGGER.error("GeneralFailureException "+e.getMessage());
					e.printStackTrace();
				} catch (CacheException e) {
					// TODO Auto-generated catch block
					LOGGER.error("CacheException "+e.getMessage());
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					LOGGER.error("JsonMappingException "+e.getMessage());
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					LOGGER.error("JsonProcessingException "+e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOGGER.error("Exception "+e.getMessage());
					e.printStackTrace();
				}
		}
		
	}
	
	private CacheInitParams getCacheConnectionParams() throws JsonMappingException, JsonProcessingException
	{
		RestTemplate restTemplate = this.restTemplateBuilder.build(); 
		String ncacheDiscoveryUrl = this.env.getProperty("NCacheDiscoveryURL"); 
		
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

}
