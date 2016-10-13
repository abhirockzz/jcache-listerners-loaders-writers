package com.wordpress.abhirockzz.jcache;

import com.wordpress.abhirockzz.jcache.JCacheExmaple;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class JCacheEventAndIntegrationTest {

    public JCacheEventAndIntegrationTest() {
    }

    static CacheManager manager;

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("hazelcast.jcache.provider.type", "server");
        manager = Caching.getCachingProvider().getCacheManager();
    }

    @AfterClass
    public static void tearDownClass() {
        manager.close();
    }
    Cache<String, String> cache;

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
        cache.close();
    }

    @Test
    public void testCacheLoaderTriggeredBeforeCacheEntryListener() throws InterruptedException {

        CountDownLatch readThruLatch = new CountDownLatch(1);
        CountDownLatch eventListenerLatch = new CountDownLatch(1);

        MutableConfiguration<String, String> configuration
                = new MutableConfiguration()
                        .setStoreByValue(false)
                        .setTypes(String.class, String.class)
                        .setCacheLoaderFactory(FactoryBuilder.factoryOf(new JCacheExmaple.ReadThroughImpl(readThruLatch)))
                        .setReadThrough(true);

        cache = manager.createCache("cache-"+UUID.randomUUID().toString(), configuration);

        CacheEntryListenerConfiguration<String, String> listenerConfiguration
                = new MutableCacheEntryListenerConfiguration<>(FactoryBuilder.factoryOf(new JCacheExmaple.MyCacheEntryListener(eventListenerLatch)),
                        null,
                        false,
                        true);

        cache.registerCacheEntryListener(listenerConfiguration);
        String notInCache = UUID.randomUUID().toString();
        String value = cache.get(notInCache);

        assertTrue(readThruLatch.await(2, TimeUnit.SECONDS));
        assertTrue(eventListenerLatch.await(2, TimeUnit.SECONDS));
        assertEquals(notInCache+" returned via read through", value) ;
    }

    @Test
    public void testCacheWriterTriggeredBeforeCacheEntryListenerForNewEntry() throws InterruptedException {

        CountDownLatch writeThruLatch = new CountDownLatch(1);
        CountDownLatch eventListenerLatch = new CountDownLatch(1);

        MutableConfiguration<String, String> configuration
                = new MutableConfiguration()
                        .setStoreByValue(false)
                        .setTypes(String.class, String.class)
                        .setCacheWriterFactory(FactoryBuilder.factoryOf(new JCacheExmaple.WriteThroughImpl(writeThruLatch)))
                        .setWriteThrough(true);

        cache = manager.createCache("cache-"+UUID.randomUUID().toString(), configuration);

        CacheEntryListenerConfiguration<String, String> listenerConfiguration
                = new MutableCacheEntryListenerConfiguration<>(FactoryBuilder.factoryOf(new JCacheExmaple.MyCacheEntryListener(eventListenerLatch)),
                        null,
                        false,
                        true);

        cache.registerCacheEntryListener(listenerConfiguration);
        
        String value = new Date().toString();
        cache.put("key1", value);

        assertTrue(writeThruLatch.await(2, TimeUnit.SECONDS));
        assertTrue(eventListenerLatch.await(2, TimeUnit.SECONDS));
        assertEquals(value, cache.get("key1"));
    }

}
