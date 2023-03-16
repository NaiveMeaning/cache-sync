package org.example.core;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.CaffeineCacheManager;
import org.example.spi.AutoCache;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CaffeineCache缓存AOP切面
 *
 * @author maxueyan
 * @date 2019/11/15
 */
@Slf4j
@Aspect
@Component
public class CaffeineCacheAop implements BeanFactoryAware {

    /**
     * SpringBean容器
     */
    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * 切面方法，优先从缓存中获取，如果缓存中没有，则继续执行原方法逻辑，尝试将方法直接结果更新到本地缓存中
     *
     * @param joinPoint 连接点
     * @return 切面方法执行结果
     */
    @Around("@annotation(org.example.spi.AutoCache)")
    public Object cacheAround(ProceedingJoinPoint joinPoint) {
        Object result = null;
        CaffeineCacheManager caffeineCacheManager = null;
        Object key = null;
        String cacheName = null;
        try {
            AutoCache autoCacheAnnotation = getMethodAutoCacheAnnotation(joinPoint);
            AutoCacheParameter parameter = getAutoCacheParameter(autoCacheAnnotation);
            if (null != parameter) {
                caffeineCacheManager = (CaffeineCacheManager) beanFactory.getBean(parameter.getCacheManager());
                key = getMethodParameterObj(joinPoint, parameter.getKeyIndex());
                cacheName = parameter.getCacheName();
                Class resultClass = parameter.getResultClass();
                result = getFromCache(caffeineCacheManager, cacheName, key, resultClass);
            }
        } catch (Exception e) {
            log.error("CaffeineCacheManager前置处理获取缓存数据异常！", e);
        }
        if (null != result) {
            log.debug("CaffeineCacheManager缓存中获取成功，key:{}", key.toString());
            return result;
        }

        Object[] args = joinPoint.getArgs();
        result = joinPoint.proceed(args);
        try {
            afterProcess(caffeineCacheManager, cacheName, key, result);
        } catch (Exception e) {
            log.error("CaffeineCacheManager后置处理异常！", e);
        }
        return result;
    }

    /**
     * 后置处理
     * 
     * @param caffeineCacheManager 缓存管理
     * @param cacheName 缓存名称
     * @param key 缓存key
     * @param result 缓存值
     */
    private void afterProcess(CaffeineCacheManager caffeineCacheManager, String cacheName, Object key, Object result) {
        if (null == caffeineCacheManager || null == cacheName || null == key) {
            return;
        }
        if (key instanceof Collection) {
            return;
        }
        if (key instanceof Map) {
            return;
        }
        caffeineCacheManager.addValue(cacheName, key, result);
    }

    /**
     * 从缓存中获取单个结果
     * 
     * @param caffeineCacheManager 缓存管理器
     * @param cacheName 缓存名称
     * @param key 缓存key
     * @param resultClass 缓存vale类型
     * @return 缓存value
     */
    @SuppressWarnings("unchecked")
    private Object getFromCache(CaffeineCacheManager caffeineCacheManager, String cacheName, Object key, Class resultClass) {
        if (null == caffeineCacheManager || null == cacheName || null == key) {
            return null;
        }
        if (key instanceof Collection) {
            Collection<Object> ids = (Collection<Object>) key;
            return getForCollection(caffeineCacheManager, cacheName, ids, resultClass);
        } else if (key instanceof Map) {
            return null;
        } else {
            return caffeineCacheManager.getValue(cacheName, key);
        }
    }

    /**
     * 从缓存集合中获取
     * 
     * @param caffeineCacheManager 缓存管理器
     * @param cacheName 缓存名称
     * @param ids 缓存ids
     * @param resultClass 缓存value类型
     * @return 缓存value
     */
    @SuppressWarnings("unchecked")
    private Object getForCollection(CaffeineCacheManager caffeineCacheManager, String cacheName, Collection<Object> ids, Class resultClass) {
        if (null == resultClass) {
            return null;
        }
        try {
            Constructor constructor = resultClass.getConstructor();
            Object cacheResult = constructor.newInstance();
            if (cacheResult instanceof Collection) {
                Collection result = (Collection) cacheResult;
                Collection<Object> distinctIds = ids.stream().distinct().collect(Collectors.toList());
                for (Object id : distinctIds) {
                    Object value = caffeineCacheManager.getValue(cacheName, id);
                    if (null == value) {
                        return null;
                    }
                    result.add(value);
                }
                log.debug("CaffeineCacheManager批量获取成功，size:{}, keys:{}", ids.size(), ids.toString());
                return cacheResult;
            }
            return null;
        } catch (Exception e) {
            log.error("CaffeineCacheManager批量获取异常，size:{}, keys:{}", ids.size(), ids.toString(), e);
            return null;
        }
    }

    /**
     * 获取缓存key
     *
     * @param joinPoint 方法链接点
     * @param keyIndex  key位置
     * @return 缓存key
     */
    private Object getMethodParameterObj(ProceedingJoinPoint joinPoint, int keyIndex) {
        Object[] args = joinPoint.getArgs();
        int size = args.length;
        if (keyIndex >= size || keyIndex < 0) {
            return null;
        }
        return args[keyIndex];
    }

    /**
     * AutoCache注解转换为参数模型
     *
     * @param autoCacheAnnotation 注解信息
     * @return 注解模型
     */
    private AutoCacheParameter getAutoCacheParameter(AutoCache autoCacheAnnotation) {
        if (null == autoCacheAnnotation) {
            return null;
        }
        String cacheManager = autoCacheAnnotation.cacheManager();
        String cacheName = autoCacheAnnotation.cacheName();
        int keyIndex = autoCacheAnnotation.keyIndex();
        if ("".equals(cacheManager) || "".equals(cacheName) || -1 == keyIndex) {
            return null;
        }
        Class resultClass = autoCacheAnnotation.resultClass();
        AutoCacheParameter parameter = new AutoCacheParameter();
        parameter.setCacheManager(cacheManager);
        parameter.setCacheName(cacheName);
        parameter.setKeyIndex(keyIndex);
        parameter.setResultClass(resultClass);
        return parameter;
    }

    /**
     * 获取自定义AutoCache注解
     *
     * @param joinPoint 连接点
     * @return AutoCache注解，null-异常
     */
    private AutoCache getMethodAutoCacheAnnotation(ProceedingJoinPoint joinPoint) {
        AutoCache autoCacheAnnotation;
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            autoCacheAnnotation = method.getAnnotation(AutoCache.class);
        } catch (Exception e) {
            log.error("CaffeineCacheManager获取自动缓存注解失败！", e);
            return null;
        }
        return autoCacheAnnotation;
    }

    /**
     * 缓存参数类型
     */
    @Data
    private static class AutoCacheParameter {
        /**
         * 缓存管理器
         */
        private String cacheManager;
        /**
         * 缓存名
         */
        private String cacheName;
        /**
         * 缓存key在方法参数中的位置
         */
        private int keyIndex;
        /**
         * 缓存Vale类型
         */
        private Class resultClass;
    }
}
