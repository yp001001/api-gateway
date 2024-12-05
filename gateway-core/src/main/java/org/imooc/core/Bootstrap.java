package org.imooc.core;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.imooc.common.config.DynamicConfigManager;
import org.imooc.common.config.ServiceDefinition;
import org.imooc.common.config.ServiceInstance;
import org.imooc.common.utils.NetUtils;
import org.imooc.common.utils.TimeUtil;
import org.imooc.core.filter.router.Configuration;
import org.imooc.gateway.config.center.api.ConfigCenter;
import org.imooc.gateway.register.center.api.RegisterCenter;
import org.imooc.gateway.register.center.api.RegisterCenterListener;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static org.imooc.common.constants.BasicConst.COLON_SEPARATOR;

/**
 * API网关启动类
 * TODO: 链路追踪，实时监控，熔断，限流 https://mp.weixin.qq.com/s/iITqdIiHi3XGKq6u6FRVdg
 */
@Slf4j
public class Bootstrap
{
    public static void main( String[] args )
    {
        //加载网关核心静态配置
        Config config = ConfigLoader.getInstance().load(args);
        System.out.println(config.getPort());

        Configuration configuration = Configuration.getInstance();
        // TODO：暂时只支持nacos注册中心
        configuration.setRegisterType("nacos", config.getRegistryAddress());

        //插件初始化
        //配置中心管理器初始化，连接配置中心，监听配置的新增、修改、删除
        ServiceLoader<ConfigCenter> serviceLoader = ServiceLoader.load(ConfigCenter.class);
        Iterator<ConfigCenter> iterator = serviceLoader.iterator();
        if(!iterator.hasNext()){
            log.error("not found ConfigCenter impl");
            throw new RuntimeException("not found ConfigCenter impl");
        }
        final ConfigCenter configCenter = iterator.next();

        configCenter.init(config.getRegistryAddress(), config.getEnv());
        configCenter.subscribeRulesChange((prefixPath, rules) -> DynamicConfigManager.getInstance()
            .putAllRule(prefixPath, rules));


        //启动容器
        Container container = new Container(config);
        container.start();

        //连接注册中心，将注册中心的实例加载到本地
        final RegisterCenter registerCenter = registerAndSubscribe(config);

        //服务优雅关机
        //收到kill信号时调用
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                registerCenter.deregister(buildGatewayServiceDefinition(config),
                    buildGatewayServiceInstance(config));
                container.shutdown();
            }
        });
    }

    private static RegisterCenter registerAndSubscribe(Config config) {
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        Iterator<RegisterCenter> iterator = serviceLoader.iterator();
        if(!iterator.hasNext()){
            log.error("not found RegisterCenter impl");
            throw new RuntimeException("not found RegisterCenter impl");
        }
        final RegisterCenter registerCenter = iterator.next();
        registerCenter.init(config.getRegistryAddress(), config.getEnv());

        //构造网关服务定义和服务实例
        ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance = buildGatewayServiceInstance(config);

        //注册
        registerCenter.register(serviceDefinition, serviceInstance);

        //订阅
        registerCenter.subscribeAllServices(new RegisterCenterListener() {
            @Override
            public void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet) {
                log.info("refresh service and instance: {} {}", serviceDefinition.getUniqueId(),
                    JSON.toJSON(serviceInstanceSet));
                DynamicConfigManager manager = DynamicConfigManager.getInstance();
                manager.addServiceInstance(serviceDefinition.getServiceId(), serviceInstanceSet);
                manager.putServiceDefinition(serviceDefinition.getServiceId(),serviceDefinition);
            }
        });
        return registerCenter;
    }

    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        String localIp = NetUtils.getLocalIp();
        int port = config.getPort();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(localIp + COLON_SEPARATOR + port);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        return serviceInstance;
    }

    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setInvokerMap(Maps.newHashMap());
        serviceDefinition.setUniqueId(config.getApplicationName());
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setEnvType(config.getEnv());
        return serviceDefinition;
    }
}
