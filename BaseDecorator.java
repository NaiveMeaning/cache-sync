package org.example.core;

import com.github.benmanes.caffeine.cache.Cache;
import org.example.spi.CaffeineCache;

import java.util.Collection;
import java.util.Map;

/**
 * 基础装饰器
 *
 * @author maxueyan
 * @date 2019/11/15
 */
public class BaseDecorator implements CaffeineCache {

    protected CaffeineCache cache;

    public BaseDecorator(CaffeineCache cache) {
        this.cache = cache;
    }

    @Override
    public String getCacheName() {
        return this.cache.getCacheName();
    }

    @Override
    public String getListenPath() {
        return this.cache.getListenPath();
    }

    @Override
    public int getMaxSize() {
        return this.cache.getMaxSize();
    }

    @Override
    public int getExpireAfterWrite() {
        return this.cache.getExpireAfterWrite();
    }

    @Override
    public Cache getCache() {
        return this.cache.getCache();
    }

    @Override
    public String getCacheStat() {
        return this.cache.getCacheStat();
    }

    @Override
    public Object getValue(Object id) {
        return this.cache.getValue(id);
    }

    @Override
    public <K> Map<K, Object> getValues(Collection<K> ids) {
        return this.cache.getValues(ids);
    }

    @Override
    public <K, T> T getValueAndFormat(K id, Class<T> formatClass) {
        return this.cache.getValueAndFormat(id, formatClass);
    }

    @Override
    public <K, T> Map<K, T> getValuesAndFormat(Collection<K> ids, Class<T> formatClass) {
        return this.cache.getValuesAndFormat(ids, formatClass);
    }

    @Override
    public <k> void addValue(k id, Object value) {
        this.cache.addValue(id, value);
    }

    @Override
    public <K, V> int addValues(Map<K, V> values) {
        return this.cache.addValues(values);
    }

    @Override
    public void removeKey(Object id) {
        this.cache.removeKey(id);
    }
}
