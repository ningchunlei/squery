package org.squery.index;

import org.apache.commons.lang.StringUtils;

import com.alisoft.xplatform.asf.cache.ICacheManager;
import com.alisoft.xplatform.asf.cache.IMemcachedCache;
import com.alisoft.xplatform.asf.cache.memcached.CacheUtil;
import com.alisoft.xplatform.asf.cache.memcached.MemcachedCacheManager;

public class MemCachedUtil {
	private static ICacheManager<IMemcachedCache> manager;

	static {
		manager = CacheUtil.getCacheManager(IMemcachedCache.class,
				MemcachedCacheManager.class.getName());

		String configFile = Config.getProperty("memcached.config", "cache");
		if(StringUtils.isNotBlank(configFile))
			manager.setConfigFile(configFile);

		manager.start();
	}

	public static IMemcachedCache getCache() {
		String defaultClient = Config.getProperty("memcached.client.default", "cache");
		IMemcachedCache cache = MemCachedUtil.manager.getCache(defaultClient);
		return cache;
	}

	public static IMemcachedCache getCache(String clientName) {
		IMemcachedCache cache = MemCachedUtil.manager.getCache(clientName);
		return cache;
	}

	public static ICacheManager<IMemcachedCache> getCacheManager() {
		return MemCachedUtil.manager;
	}
}
