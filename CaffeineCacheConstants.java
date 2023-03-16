package org.example.constant;

/**
 * 缓存字符常量
 * 
 * @author maxueyan
 * @date 2019/11/15
 */
public class CaffeineCacheConstants {

    /**
     * 默认缓存管理器
     */
    public static final String DEFAULT_CACHE_MANAGER = "caffeineCacheManager";
    /**
     * 缓存开关配置名
     */
    public static final String DISABLE_CACHE_CONFIG = "caffeine_cache_disable";
    
    /**
     * 缓存默认配置
     */
    public static int DEFAULT_CAFFEINE_CACHE_EXPIRE_AFTER_WRITE_TIME = 1;
    public static int DEFAULT_CAFFEINE_CACHE_MAX_SIZE = 100;
    public static int DEFAULT_CAFFEINE_CACHE_MAX_SIZE_EXTREME = 5000;

    /**
     * 默认延时
     */
    public static int DEFAULT_DELAY_TIME = 500;
    
}
