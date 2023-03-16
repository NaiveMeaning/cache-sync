package org.example.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;


import lombok.extern.slf4j.Slf4j;
import org.example.spi.CaffeineCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * caffeineCache实现
 * 
 * @author maxueyan
 * @date 2019/11/15
 */
@Slf4j
public class CaffeineCacheImpl implements CaffeineCache {

    /**
     * 缓存名
     */
    private String cacheName;
    /**
     * 监听路径
     */
    private String path;
    /**
     * 缓存大小
     */
    private int maxSize;
    /**
     * 失效时间
     */
    private int expireAfterWrite;
    /**
     * caffeine缓存
     */
    private Cache<String, Object> cache;

    public CaffeineCacheImpl(String cacheName, int maxSize, int expireAfterWrite) {
        this.cacheName = cacheName;
        this.maxSize = maxSize;
        this.expireAfterWrite = expireAfterWrite;
        this.cache = Caffeine.newBuilder().maximumSize(this.maxSize).recordStats().expireAfterWrite(this.expireAfterWrite, TimeUnit.SECONDS).build();
    }

    public CaffeineCacheImpl(String cacheName, String path, int maxSize, int expireAfterWrite) {
        this.cacheName = cacheName;
        this.path = path;
        this.maxSize = maxSize;
        this.expireAfterWrite = expireAfterWrite;
        this.cache = Caffeine.newBuilder().maximumSize(this.maxSize).recordStats().expireAfterWrite(this.expireAfterWrite, TimeUnit.SECONDS).build();
    }

    @Override
    public String getCacheName() {
        return this.cacheName;
    }

    @Override
    public String getListenPath() {
        return this.path;
    }

    @Override
    public int getMaxSize() {
        return this.maxSize;
    }

    @Override
    public int getExpireAfterWrite() {
        return this.expireAfterWrite;
    }

    @Override
    public Cache getCache() {
        return this.cache;
    }

    @Override
    public String getCacheStat() {
        return this.cache.stats().toString();
    }

    @Override
    public <K> Object getValue(K id) {
        if (null == id || null == this.cache) {
            return null;
        }
        try {
            String coveredId = String.valueOf(id);
            return this.cache.getIfPresent(coveredId);
        } catch (Exception e) {
            log.error("获取缓存异常, id={}", id, e);
            return null;
        }
    }

    @Override
    public <K> Map<K, Object> getValues(Collection<K> ids) {
        if (null == ids || null == this.cache) {
            return null;
        }
        return batchGetValues(ids);
    }

    @Override
    public <K, T> T getValueAndFormat(K id, Class<T> formatClass) {
        if (null == id || null == this.cache) {
            return null;
        }
        Object value = null;
        try {
            String coveredId = String.valueOf(id);
            value = this.cache.getIfPresent(coveredId);
            return JsonUtils.findObject(String.valueOf(value), formatClass);
        } catch (Exception e) {
            log.error("[JsonUtils#findObject] 反序列化json失败, jsonStr={}, class name={}", value, formatClass.getName());
            return null;
        }
    }

    @Override
    public <K, T> Map<K, T> getValuesAndFormat(Collection<K> ids, Class<T> formatClass) {
        if (null == ids || null == this.cache) {
            return null;
        }
        Map<K, T> formatResult = new HashMap<>(ids.size());
        try {
            Map<K, Object> result = batchGetValues(ids);
            for (K key : result.keySet()) {
                Object value = result.get(key);
                T resultInstance = JsonUtils.findObject(String.valueOf(value), formatClass);
                if (null != resultInstance) {
                    formatResult.put(key, resultInstance);
                }
            }
        } catch (Exception e) {
            log.error("批量格式化获取数据错误, ids:{}", ids.toString(), e);
        }
        return formatResult;
    }

    @Override
    public <K> void addValue(K id, Object value) {
        if (null == id || null == this.cache || null == value) {
            return;
        }
        String coveredId = String.valueOf(id);
        this.cache.put(coveredId, value);
    }

    @Override
    public <K, V> int addValues(Map<K, V> values) {
        if (null == values || null == this.cache) {
            return 0;
        }
        int size = 0;
        for (K key : values.keySet()) {
            V value = values.get(key);
            String coveredId = String.valueOf(key);
            if (null == key || null == value) {
                continue;
            }
            this.cache.put(coveredId, value);
            size++;
        }
        return size;
    }

    @Override
    public void removeKey(Object id) {
        if (null == id || null == this.cache) {
            return;
        }
        String coveredId = String.valueOf(id);
        this.cache.invalidate(coveredId);
    }

    private <K> Map<K, Object> batchGetValues(Collection<K> ids) {
        Map<K, Object> result = new HashMap<>();
        if (null == ids || null == this.cache) {
            return result;
        }
        try {
            for (K id : ids) {
                String coveredId = String.valueOf(id);
                Object value = this.cache.getIfPresent(coveredId);
                if (null != value) {
                    result.put(id, value);
                }
            }
        } catch (Exception e) {
            log.error("批量获取数据错误, ids:{}", ids.toString(), e);
        }
        return result;
    }
}
