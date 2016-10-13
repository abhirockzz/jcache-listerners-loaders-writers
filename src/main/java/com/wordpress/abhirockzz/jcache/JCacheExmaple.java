package com.wordpress.abhirockzz.jcache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import javax.cache.Cache;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

public class JCacheExmaple {

  
    public static class ReadThroughImpl implements CacheLoader<String, String>, Serializable {

        public ReadThroughImpl() {
        }
        private CountDownLatch latch;

        public ReadThroughImpl(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public String load(String k) throws CacheLoaderException {
            Logger.getLogger(ReadThroughImpl.class.getName()).info("key queried - " + k);
            if (latch != null) {
                latch.countDown();
            }
            return k + " returned via read through";
        }

        @Override
        public Map<String, String> loadAll(Iterable<? extends String> itrbl) throws CacheLoaderException {
            return null;
        }

    }

    public static class WriteThroughImpl implements CacheWriter<String, String>, Serializable {

        public WriteThroughImpl() {
        }
        private CountDownLatch latch;

        public WriteThroughImpl(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void write(Cache.Entry<? extends String, ? extends String> entry) throws CacheWriterException {
            Logger.getLogger(WriteThroughImpl.class.getName()).info("entry created/updated. new value -- " + entry.getValue());
            if (latch != null) {
                latch.countDown();
            }

        }

        @Override
        public void writeAll(Collection<Cache.Entry<? extends String, ? extends String>> clctn) throws CacheWriterException {

        }

        @Override
        public void delete(Object o) throws CacheWriterException {

        }

        @Override
        public void deleteAll(Collection<?> clctn) throws CacheWriterException {
        }

    }

    /**
     * A simple cache listener example
     */
    public static class MyCacheEntryListener implements CacheEntryCreatedListener<String, String>,
            CacheEntryUpdatedListener<String, String>, Serializable {

        public MyCacheEntryListener() {
        }

        private CountDownLatch latch;

        public MyCacheEntryListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends String, ? extends String> entryEvent : cacheEntryEvents) {
                Logger.getLogger(MyCacheEntryListener.class.getName()).info("Created key: " + entryEvent.getKey() + " with value: " + entryEvent.getValue());
            }

            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends String>> cacheEntryEvents)
                throws CacheEntryListenerException {
            for (CacheEntryEvent<? extends String, ? extends String> entryEvent : cacheEntryEvents) {
                Logger.getLogger(MyCacheEntryListener.class.getName()).info("Updated key: " + entryEvent.getKey() + " with value: " + entryEvent.getValue());
            }

            if (latch != null) {
                latch.countDown();
            }
        }
    }
}
