package org.imooc.common.config;

import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.units.qual.C;
import org.imooc.common.exception.ResponseException;
import org.imooc.common.route.RouteHandler;
import org.imooc.common.route.RouteTrie;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.imooc.common.enums.ResponseCode.REQUEST_PARSE_ERROR;

/**
 * 动态服务缓存配置管理类
 */
public class DynamicConfigManager {

    //	服务的定义集合：uniqueId代表服务的唯一标识
    private ConcurrentHashMap<String /* uniqueId */ , ServiceDefinition> serviceDefinitionMap = new ConcurrentHashMap<>();

    //	服务的实例集合：uniqueId与一对服务实例对应
    private ConcurrentHashMap<String /* uniqueId */ , Set<ServiceInstance>> serviceInstanceMap = new ConcurrentHashMap<>();

    //	规则集合
    private ConcurrentHashMap<String /* ruleId */ , Rule> ruleMap = new ConcurrentHashMap<>();

    //路径以及规则集合
    private ConcurrentHashMap<String /* 路径 */ , Rule> pathRuleMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String /* 服务名 */ , List<Rule>> serviceRuleMap = new ConcurrentHashMap<>();

    // 网关配置信息
    private ConcurrentHashMap<String, ConfigServerData> configServerDataMap = new ConcurrentHashMap<>();

    // 服务路由配置信息
    private ConcurrentHashMap<String, Map<String, Rule>> ruleConfig = new ConcurrentHashMap<>();

    private RouteTrie routeTrie;

    private DynamicConfigManager() {
        routeTrie = new RouteTrie();
    }

    public void setConfigServerDatas(List<ConfigServerData> configServerDataList) {
        Map<String, ConfigServerData> serverConfigMap =
                configServerDataList.stream().collect(Collectors.toMap(ConfigServerData::getServerPrefixPath, Function.identity()));
        if (null != serverConfigMap && serverConfigMap.size() > 0)
            configServerDataMap = new ConcurrentHashMap<>(serverConfigMap);
    }

    public ConcurrentHashMap<String, ConfigServerData> getConfigServerDataMap() {
        return configServerDataMap;
    }

    private static class SingletonHolder {
        private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
    }


    /***************** 	对服务定义缓存进行操作的系列方法 	***************/

    public static DynamicConfigManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void putServiceDefinition(String serverPrefixName,
                                     ServiceDefinition serviceDefinition) {

        serviceDefinitionMap.put(serverPrefixName, serviceDefinition);
        ;
    }

    public ServiceDefinition getServiceDefinition(String serverPrefixName) {
        return serviceDefinitionMap.get(serverPrefixName);
    }


    public void removeServiceDefinition(String uniqueId) {
        serviceDefinitionMap.remove(uniqueId);
    }

    public ConcurrentHashMap<String, ServiceDefinition> getServiceDefinitionMap() {
        return serviceDefinitionMap;
    }

    /***************** 	对服务实例缓存进行操作的系列方法 	***************/

    public Set<ServiceInstance> getServiceInstanceByUniqueId(String uniqueId, boolean gray) {
        Set<ServiceInstance> serviceInstances = serviceInstanceMap.get(uniqueId);
        if (CollectionUtils.isEmpty(serviceInstances)) {
            return Collections.emptySet();
        }

        if (gray) {
            return serviceInstances.stream()
                    .filter(ServiceInstance::isGray)
                    .collect(Collectors.toSet());
        }

        return serviceInstances;
    }

    public void addServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
        set.add(serviceInstance);
    }

    public void addServiceInstance(String uniqueId, Set<ServiceInstance> serviceInstanceSet) {
        serviceInstanceMap.put(uniqueId, serviceInstanceSet);
    }

    public void updateServiceInstance(String uniqueId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> it = set.iterator();
        while (it.hasNext()) {
            ServiceInstance is = it.next();
            if (is.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
                it.remove();
                break;
            }
        }
        set.add(serviceInstance);
    }

    public void removeServiceInstance(String uniqueId, String serviceInstanceId) {
        Set<ServiceInstance> set = serviceInstanceMap.get(uniqueId);
        Iterator<ServiceInstance> it = set.iterator();
        while (it.hasNext()) {
            ServiceInstance is = it.next();
            if (is.getServiceInstanceId().equals(serviceInstanceId)) {
                it.remove();
                break;
            }
        }
    }

    public void removeServiceInstancesByUniqueId(String uniqueId) {
        serviceInstanceMap.remove(uniqueId);
    }


    /***************** 	对规则缓存进行操作的系列方法 	***************/

    public void putRule(String ruleId, Rule rule) {
        ruleMap.put(ruleId, rule);
    }

    // 将动态路由匹配与固定路由匹配分开，固定路由匹配时间复杂度为O(1)
    public void putAllRule(String serverPrefixPath, List<Rule> ruleList) {

        Map<String, Rule> ruleMap = new HashMap<>();
        for (Rule rule : ruleList) {
            for (Rule.PathRule path : rule.getPaths()) {
                path.setRule(rule);
                if (path.getPath().contains("{") && path.getPath().contains("}")) {
                    routeTrie.addRoute(path.getPath(), new RouteHandler<String, Rule.PathRule>() {
                        @Override
                        public Rule.PathRule apply(String input) {
                            return path;
                        }
                    });
                } else {
                    String key = serverPrefixPath + "." + path.getPath();
                    ruleMap.put(key, rule);
                }
            }
        }
        ruleConfig.put(serverPrefixPath, ruleMap);

    }

    public Rule getRule(String ruleId) {
        return ruleMap.get(ruleId);
    }

    public void removeRule(String ruleId) {
        ruleMap.remove(ruleId);
    }

    public ConcurrentHashMap<String, Rule> getRuleMap() {
        return ruleMap;
    }

    public Rule getRuleByPath(String path) {
        String[] urlSplit = path.split("\\.");
        if (urlSplit.length != 2) {
            throw new ResponseException(REQUEST_PARSE_ERROR);
        }
        Map<String, Rule> ruleMap = ruleConfig.get(urlSplit[0]);
        if (null == ruleMap || ruleMap.size() == 0) {
            return null;
        }
        // 先完全匹配
        Rule rule = ruleMap.get(path);
        if (null == rule) {
            RouteHandler routeHandler = routeTrie.matchRoute(urlSplit[1]);
            if (null != routeHandler) {
                return ((Rule.PathRule) routeHandler.apply(urlSplit[1])).getRule();
            }
        }
        return rule;
    }


    public Rule.PathRule getRulePath(String path) {
        String[] urlSplit = path.split("\\.");
        if (urlSplit.length != 2) {
            throw new ResponseException(REQUEST_PARSE_ERROR);
        }
        Map<String, Rule> ruleMap = ruleConfig.get(urlSplit[0]);
        if (null == ruleMap || ruleMap.size() == 0) {
            return null;
        }
        // 先完全匹配
        Rule rule = ruleMap.get(path);
        if (null == rule) {
            RouteHandler routeHandler = routeTrie.matchRoute(urlSplit[1]);
            if (null != routeHandler) {
                return (Rule.PathRule) routeHandler.apply(urlSplit[1]);
            }
        } else {
            for (Rule.PathRule rulePath : rule.getPaths()) {
                if(urlSplit[1].equals(rulePath.getPath())){
                    return rulePath;
                }
            }
        }
        return null;
    }

    public List<Rule> getRuleByServiceId(String serviceId) {
        return serviceRuleMap.get(serviceId);
    }
}
