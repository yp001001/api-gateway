package org.imooc.core.filter.limit;

import org.imooc.common.config.DynamicConfigManager;
import org.imooc.common.enums.ResponseCode;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: yp
 * @date: 2024/12/5 16:49
 * @description:固定窗口限流（目前是单机，可以实现为分布式情况，保存在Redis中）
 */
public class FixedWindowLimitRequest implements LimitRequest{

    private final long windowSizeMillis; // 窗口大小（毫秒）
    private final int maxRequests; // 每个窗口的最大请求数
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<>();


    public FixedWindowLimitRequest(long windowSizeMillis, int maxRequests) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequests = maxRequests;
    }

    // 判断请求是否允许
    public boolean tryRequest(String key) {
        long currentTime = System.currentTimeMillis();

        // 初始化窗口起始时间和计数器
        startTimes.computeIfAbsent(key, k -> currentTime);
        requestCounts.computeIfAbsent(key, k -> new AtomicInteger(0));

        // 获取当前窗口的起始时间
        long windowStartTime = startTimes.get(key);

        // 如果当前时间超过窗口时间，重置窗口
        if (currentTime - windowStartTime >= windowSizeMillis) {
            startTimes.put(key, currentTime);
            requestCounts.put(key, new AtomicInteger(1)); // 重置计数器
            return true; // 第一次请求总是允许
        } else {
            // 如果在窗口内，检查计数
            AtomicInteger currentCount = requestCounts.get(key);
            if (currentCount.get() < maxRequests) {
                currentCount.incrementAndGet();
                return true; // 允许请求
            } else {
                return false; // 拒绝请求
            }
        }
    }

    // 清理过期数据
    public void cleanUp() {
        long currentTime = System.currentTimeMillis();
        startTimes.keySet().removeIf(key -> currentTime - startTimes.get(key) >= windowSizeMillis);
        requestCounts.keySet().removeIf(key -> !startTimes.containsKey(key));
    }



    @Override
    public boolean limit(GatewayContext context) {
        return tryRequest(context.getRequest().getPath());
    }
}
