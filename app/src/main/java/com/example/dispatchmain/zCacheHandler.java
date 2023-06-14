package com.example.dispatchmain;

import java.util.HashMap;
import java.util.Map;

public class zCacheHandler
{
    private static zCacheHandler instance;
    private Map<String, Object> cache;

    public zCacheHandler() {
        cache = new HashMap<>();
    }

    public static zCacheHandler getInstance() {
        if (instance == null) {
            instance = new zCacheHandler();
        }
        return instance;
    }

    public void addToCache(String key, Object value) {
        cache.put(key, value);
    }

    public Object getFromCache(String key) {
        return cache.get(key);
    }

    public void removeFromCache(String key) {
        cache.remove(key);
    }

    public void clearCache() {
        cache.clear();
    }
}

