package com.example.ncachejavaclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alachisoft.ncache.web.caching.CacheInitParams;
import com.alachisoft.ncache.web.caching.CacheServerInfo;

public class CacheInfo {
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheInfo.class);

	private String cacheID;
	private CacheInitParams connectionParams;

	public CacheInfo(String cacheId, CacheInitParams connParams) {
		this.cacheID = cacheId;
		this.connectionParams = connParams;
		
		LOGGER.info("The cache ID is "+this.cacheID);
		LOGGER.info("Here are the servers:\n");
		
		CacheServerInfo[] servers = this.connectionParams.getServerList();
		
		for (int i = 0; i < servers.length; i++)
		{
			LOGGER.info("Server "+i+ ": " + servers[i].getName() + " " + servers[i].getPort());
		}
	}

	public String getCacheId() {
		return this.cacheID;
	}

	public CacheInitParams getConnectionParams() {
		return this.connectionParams;
	}
	 
}
