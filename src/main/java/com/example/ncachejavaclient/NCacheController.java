package com.example.ncachejavaclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alachisoft.ncache.runtime.caching.Tag;
import com.alachisoft.ncache.runtime.exceptions.CacheException;
import com.alachisoft.ncache.runtime.exceptions.ConfigurationException;
import com.alachisoft.ncache.runtime.exceptions.GeneralFailureException;
import com.alachisoft.ncache.web.caching.Cache;
import com.alachisoft.ncache.web.caching.CacheItem;
import com.alachisoft.ncache.web.caching.CacheServerInfo;
import com.alachisoft.ncache.web.caching.NCache;

@RestController
public class NCacheController {
	
	private CacheInfo cacheInfo;
	private Cache cache;
	private Tag tag;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NCacheController.class);
	
	@Autowired
	public NCacheController(CacheInfo cacheinf) {
		this.cacheInfo = cacheinf;
		this.tag = new Tag("items");
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
					cache = NCache.initializeCache(cacheInfo.getCacheId(), cacheInfo.getConnectionParams());
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
				} catch (Exception e) {
					// TODO Auto-generated catch block
					LOGGER.error("Exception "+e.getMessage());
					e.printStackTrace();
				}
		}
		
	}
}
