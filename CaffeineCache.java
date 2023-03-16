package org.example.spi;

import com.github.benmanes.caffeine.cache.Cache;

import java.util.Collection;
import java.util.Map;

/**
 * CaffeineCache
 * 
 * @author maxueyan
 * @date 2019/11/15
 */
public interface CaffeineCache {

    /**
     * 获取缓存Key名称
     * 
     * @return Key名称
     */
    String getCacheName();

    /**
     * 获取缓存变更监听路径
     * 
     * @return 监听路径
     */
    String getListenPath();

    /**
     * 获取最大数量
     * 
     * @return 缓存最大数量
     */
    int getMaxSize();

    /**
     * 获取缓存失效时间
     * 
     * @return 失效时间
     */
    int getExpireAfterWrite();

    /**
     * 获取缓存对象
     * 
     * @return 缓存
     */
    Cache getCache();

    /**
     * 获取缓存统计信息
     * 
     * @return 统计信息
     */
    String getCacheStat();

    /**
     * 根据单个Key获取缓存值
     * 
     * @param id 缓存Key
     * @param <K> key类型
     * @return 缓存值
     */
    <K> Object getValue(K id);

    /**
     * 批量获取缓存值
     * 
     * @param ids 缓存Key
     * @param <K> key类型
     * @return 缓存key - 缓存值
     */
    <K> Map<K, Object> getValues(Collection<K> ids);

    /**
     * 根据缓存Key和指定数据类型，获取缓存值
     * 
     * @param id 缓存id
     * @param formatClass 数据类型
     * @param <K> key类型
     * @param <T> value类型
     * @return 缓存key - 缓存数据vale
     */
    <K, T> T getValueAndFormat(K id, Class<T> formatClass);

    /**
     * 批量查询缓存
     * 
     * @param ids 缓存id
     * @param formatClass 缓存value类型
     * @param <K> key类型
     * @param <T> value类型
     * @return 缓存key - 缓存数据value
     */
    <K, T> Map<K, T> getValuesAndFormat(Collection<K> ids, Class<T> formatClass);

    /**
     * 添加单个缓存
     * 
     * @param id 缓存id
     * @param value 缓存值
     * @param <K> key类型
     */
    <K> void addValue(K id, Object value);

    /**
     * 批量添加缓存
     * 
     * @param values 缓存key和对应value
     * @param <K> key类型
     * @param <V> value类型
     * @return 变更行数
     */
    <K, V> int addValues(Map<K, V> values);

    /**
     * 从缓存删除元素
     * 
     * @param id 缓存key
     * @param <K> key类型
     */
    <K> void removeKey(K id);
}
