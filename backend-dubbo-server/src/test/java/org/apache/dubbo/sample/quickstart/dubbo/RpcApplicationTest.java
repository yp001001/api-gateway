package org.apache.dubbo.sample.quickstart.dubbo;

import javafx.application.Application;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * @author: yp
 * @date: 2024/12/4 18:01
 * @description:
 */
public class RpcApplicationTest {

    public static void main(String[] args) {
        ApplicationConfig applicationConfig = new ApplicationConfig("dubbo-consumer");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("nacos://127.0.0.1:8848?username=nacos&password=nacos");
        // 创建服务引用配置
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        // 设置接口
        referenceConfig.setInterface("org.apache.dubbo.sample.quickstart.dubbo.interfaces.IActivityBooth");
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setGeneric("true");
        referenceConfig.setVersion("1.0.0");
        // 暂时写死
        referenceConfig.setTimeout(5000);
        GenericService genericService = referenceConfig.get();
        Object response = genericService.$invoke("sayHi", new String[]{"java.lang.String"}, new Object[]{"张三"});
        System.out.println("=============>" + response);
    }

}
