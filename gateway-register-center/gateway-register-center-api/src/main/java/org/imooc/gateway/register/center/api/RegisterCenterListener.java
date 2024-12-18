package org.imooc.gateway.register.center.api;

import org.imooc.common.config.ServiceDefinition;
import org.imooc.common.config.ServiceInstance;

import java.util.Set;

public interface RegisterCenterListener {

    void onChange(ServiceDefinition serviceDefinition,
                  Set<ServiceInstance> serviceInstanceSet);
}
