package org.imooc.core.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.imooc.common.config.Rule;
import org.imooc.common.constants.FilterConst;
import org.imooc.core.context.GatewayContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 过滤器工厂实现类
 * @USER: WuYang
 * @DATE: 2023/3/12 20:05
 */
@Slf4j
public class GatewayFilterChainFactory implements FilterFactory {

    private static class SingletonInstance {
        private static final GatewayFilterChainFactory INSTANCE = new GatewayFilterChainFactory();
    }

    public static GatewayFilterChainFactory getInstance() {
        return SingletonInstance.INSTANCE;
    }

    private Cache<String, GatewayFilterChain> chainCache = Caffeine.newBuilder().recordStats().expireAfterWrite(10, TimeUnit.MINUTES).build();


    private Map<String, Filter> processorFilterIdMap = new ConcurrentHashMap<>();

    public GatewayFilterChainFactory() {
        ServiceLoader<Filter> serviceLoader = ServiceLoader.load(Filter.class);
        Iterator<Filter> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            Filter filter = iterator.next();
            FilterAspect annotation = filter.getClass().getAnnotation(FilterAspect.class);
            log.info("load filter success:{},{},{},{}", filter.getClass(),
                    annotation.id(), annotation.name(), annotation.order());
            if (annotation != null) {
                //添加到过滤集合
                String filterId = annotation.id();
                if (StringUtils.isEmpty(filterId)) {
                    filterId = filter.getClass().getName();
                }
                processorFilterIdMap.put(filterId, filter);
            }
        }
//        serviceLoader.stream().forEach(filterProvider -> {
//            Filter filter = filterProvider.get();
//            FilterAspect annotation = filter.getClass().getAnnotation(FilterAspect.class);
//            log.info("load filter success:{},{},{},{}",filter.getClass(),
//                    annotation.id(),annotation.name(),annotation.order());
//            if(annotation != null){
//                //添加到过滤集合
//                String filterId = annotation.id();
//                if(StringUtils.isEmpty(filterId)){
//                    filterId = filter.getClass().getName();
//                }
//                processorFilterIdMap.put(filterId,filter);
//            }
//        });

    }

    public static void main(String[] args) {
        new GatewayFilterChainFactory();
    }


    @Override
    public GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception {
        return chainCache.get(ctx.getRule().getId(), k -> doBuildFilterChain(ctx.getRulePath()));
    }


    public GatewayFilterChain doBuildFilterChain(Rule.PathRule pathRule) {
        Rule rule = pathRule.getRule();
        GatewayFilterChain chain = new GatewayFilterChain();
        List<Filter> filters = new ArrayList<>();
        filters.add(getFilterInfo(FilterConst.GRAY_FILTER_ID));
        filters.add(getFilterInfo(FilterConst.MONITOR_FILTER_ID));
        filters.add(getFilterInfo(FilterConst.MONITOR_END_FILTER_ID));
        filters.add(getFilterInfo(FilterConst.MOCK_FILTER_ID));
        filters.add(getFilterInfo(FilterConst.LOAD_BALANCE_FILTER_ID));
        //todo:前面的过滤器没有添加条件直接进行了添加

        // 根据条件添加限流过滤器
        if (pathRule.getLimit()) {
            filters.add(getFilterInfo(FilterConst.LIMIT_FILTER_ID));
        }

        if (rule != null) {
            Set<Rule.FilterConfig> filterConfigs = rule.getFilterConfigs();
            Iterator iterator = filterConfigs.iterator();
            Rule.FilterConfig filterConfig;
            while (iterator.hasNext()) {
                filterConfig = (Rule.FilterConfig) iterator.next();
                if (filterConfig == null) {
                    continue;
                }
                String filterId = filterConfig.getId();
                if (StringUtils.isNotEmpty(filterId) && getFilterInfo(filterId) != null) {
                    Filter filter = getFilterInfo(filterId);
                    filters.add(filter);
                }
            }
        }
        // 添加路由过滤器-这是最后一步
        if (rule.getProtocol().equals("http")) {
            filters.add(getFilterInfo(FilterConst.ROUTER_FILTER_ID));
        }else if(rule.getProtocol().equals("dubbo")){
            filters.add(getFilterInfo(FilterConst.ROUTER_DUBBO_FILTER_ID));
        }
        //排序
        filters.sort(Comparator.comparingInt(Filter::getOrder));
        //添加到链表中
        chain.addFilterList(filters);
        return chain;
    }

    @Override
    public Filter getFilterInfo(String filterId) {
        return processorFilterIdMap.get(filterId);
    }
}
