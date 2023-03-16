package org.example.spi;

import org.example.constant.CaffeineCacheConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本地缓存切面注解
 * 
 * @author maxueyan
 * @date 2019/11/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AutoCache {

    /**
     * 缓存管理器
     */
    String cacheManager() default CaffeineCacheConstants.DEFAULT_CACHE_MANAGER;

    /**
     * 缓存名
     */
    String cacheName() default "";

    /**
     * key在方法参数中位置
     */
    int keyIndex() default -1;

    /**
     * 方法返回结果类型
     */
    Class resultClass();
    
}
