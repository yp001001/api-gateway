package org.imooc.gateway.client.support;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.imooc.common.config.ServiceDefinition;
import org.imooc.common.config.ServiceInstance;
import org.imooc.gateway.client.core.ApiProperties;
import org.imooc.gateway.register.center.api.RegisterCenter;

import java.util.Iterator;
import java.util.ServiceLoader;

@Slf4j
public abstract class AbstractClientRegisterManager {
    @Getter
    private ApiProperties apiProperties;

    private RegisterCenter registerCenter;

    protected AbstractClientRegisterManager(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;

        //初始化注册中心对象
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        Iterator<RegisterCenter> iterator = serviceLoader.iterator();
        if(iterator.hasNext()){
            registerCenter = iterator.next();
            registerCenter.init(apiProperties.getRegisterAddress(), apiProperties.getEnv());
        }else{
            throw  new RuntimeException("not found RegisterCenter impl");
        }


    }

    protected void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        registerCenter.register(serviceDefinition, serviceInstance);
    }
}
