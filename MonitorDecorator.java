package org.example.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.kuaikan.common.perfcounter.PerfCounter;
import com.kuaikan.common.perfcounter.common.CounterGaugeMetric;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.spi.CaffeineCache;
import org.springframework.util.CollectionUtils;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;

/**
 * 缓存统计装饰器
 * 
 * @author maxueyan
 * @date 2019/11/15
 */
@Slf4j
public class MonitorDecorator extends BaseDecorator implements CaffeineCache {

    private static final String TAG_PATTERN = "name=%s,type=%s";
    private static final String METRIC_NAME = "CAFFEINE_MONITOR";
    private static final String REQUEST_TAG = "request";
    private static final String HIT_TAG = "hit";
    private static final String MISS_TAG = "miss";
    private static final String REMOVE_TAG = "remove";
    private static final String DESC = "%s本地缓存统计信息";
    private static final String STAT_FORMAT = "cacheName:(%s), stat:(%s), estimatedSize:(%s), hitRate:(%s)";

    /**
     * 计数上报组件
     */
    private CounterGaugeMetric counterGaugeMetric;

    public MonitorDecorator(CaffeineCache cache) {
        super(cache);
        String name = this.cache.getCacheName();
        this.counterGaugeMetric = new CaffeineCacheMonitorMetric(METRIC_NAME, TAG_PATTERN, String.format(DESC, name));
    }

    @Override
    public Object getValue(Object id) {
        Object result = cache.getValue(id);
        monitor(result);
        return result;
    }

    @Override
    public <K> Map<K, Object> getValues(Collection<K> ids) {
        Map<K, Object> result = cache.getValues(ids);
        monitorBatch(ids, result);
        return result;
    }

    @Override
    public <K, T> T getValueAndFormat(K id, Class<T> formatClass) {
        T result = cache.getValueAndFormat(id, formatClass);
        monitor(result);
        return result;
    }

    @Override
    public <K, T> Map<K, T> getValuesAndFormat(Collection<K> ids, Class<T> formatClass) {
        Map<K, T> result = cache.getValuesAndFormat(ids, formatClass);
        monitorBatch(ids, result);
        return result;
    }

    @Override
    public void removeKey(Object id) {
        this.cache.removeKey(id);
        monitorRemove(id);
    }

    @Override
    public String getCacheStat() {
        if (null == this.cache) {
            log.info("Cache is null in getCacheStat!");
            return StringUtils.EMPTY;
        }
        Cache cache = this.cache.getCache();
        CacheStats stats = cache.stats();
        String name = this.cache.getCacheName();
        try {
            DecimalFormat df = new DecimalFormat("#.##%");
            return String.format(STAT_FORMAT, name, stats, cache.estimatedSize(), df.format(stats.hitRate()));
        } catch (Exception e) {
            log.error("Get cache stats exception! name:{}", name, e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * 单次统计
     */
    private <T> void monitor(T value) {
        int requestSize = 1;
        int hitSize = 0;
        if (null != value) {
            hitSize = 1;
        }
        if (null != this.counterGaugeMetric) {
            commonMonitor(requestSize, hitSize, requestSize - hitSize);
        }
    }

    /**
     * 批量统计
     */
    private <K, T> void monitorBatch(Collection<K> ids, Map<K, T> result) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        if (null != this.counterGaugeMetric) {
            int requestSize = ids.size();
            int hitSize = result == null ? 0 : result.size();
            commonMonitor(requestSize, hitSize, requestSize - hitSize);
        }
    }

    /**
     * 通用统计信息
     * 
     * @param request 请求数
     * @param hit 命中数
     * @param miss 未命中数
     */
    private void commonMonitor(int request, int hit, int miss) {
        String name = this.cache.getCacheName();
        Object[] requestObj = new Object[] { name, REQUEST_TAG };
        Object[] hitObj = new Object[] { name, HIT_TAG };
        Object[] missObj = new Object[] { name, MISS_TAG };
        PerfCounter.countGauge(request, this.counterGaugeMetric, requestObj);
        PerfCounter.countGauge(hit, this.counterGaugeMetric, hitObj);
        PerfCounter.countGauge(miss, this.counterGaugeMetric, missObj);
    }

    /**
     * 删除统计
     */
    private <K> void monitorRemove(K id) {
        log.debug("本地缓存name:{} 删除id:{}", this.cache.getCacheName(), id);
        String name = this.cache.getCacheName();
        Object[] remove = new Object[] { name, REMOVE_TAG };
        PerfCounter.countGauge(1, this.counterGaugeMetric, remove);
    }

    /**
     * 专用监控
     */
    static class CaffeineCacheMonitorMetric extends CounterGaugeMetric {
        CaffeineCacheMonitorMetric(String metricName, String tagPattern, String desc) {
            super(metricName, tagPattern, desc);
        }
    }
}
