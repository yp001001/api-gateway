package org.imooc.core.filter.router;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.imooc.common.config.Rule;
import org.imooc.common.enums.ResponseCode;
import org.imooc.common.exception.ResponseException;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.filter.Filter;
import org.imooc.core.filter.FilterAspect;
import org.imooc.core.helper.ResponseHelper;
import org.imooc.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.imooc.common.constants.FilterConst.*;

/**
 * @author: yp
 * @date: 2024/12/4 14:56
 * @description:
 */
@Slf4j
@FilterAspect(id = ROUTER_DUBBO_FILTER_ID,
        name = ROUTER_DUBBO_FILTER_NAME,
        order = ROUTER_DUBBO_FILTER_ORDER)
public class RouterDubboFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(RouterDubboFilter.class);

    // RPC注册中心配置项
    private Map<String, RegistryConfig> registryConfigMap = new ConcurrentHashMap<>();
    // RPC应用服务配置项
    private Map<String, ApplicationConfig> applicationConfigMap = new ConcurrentHashMap<>();
    // RPC泛化服务配置项
    private Map<String, ReferenceConfig<GenericService>> referenceConfigMap = new ConcurrentHashMap<>();


    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        try {
            Rule rule = ctx.getRule();
            Rule.PathRule path = ctx.getRulePath();
            String serverName = ctx.getServerName();
            String referenceKey = serverName + "." + rule.getInterfaceName() + path.getMethodName() + "." + path.getParameterType();
            ReferenceConfig<GenericService> reference = referenceConfigMap.computeIfAbsent(referenceKey, key -> {
                ApplicationConfig applicationConfig = applicationConfigMap.computeIfAbsent(serverName, applicationName -> {
                    ApplicationConfig config = new ApplicationConfig();
                    config.setName(applicationName);
                    return config;
                });
                RegistryConfig registerConf = registryConfigMap.computeIfAbsent(Configuration.getInstance().getRegisterType("nacos"), address -> {
                    RegistryConfig registryConfig = new RegistryConfig();
                    registryConfig.setAddress("nacos://"+Configuration.getInstance().getRegisterType("nacos"));
                    return registryConfig;
                });
                // 创建服务引用配置
                ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
                // 设置接口
                referenceConfig.setInterface(rule.getInterfaceName());
                referenceConfig.setRegistry(registerConf);
                referenceConfig.setApplication(applicationConfig);
                referenceConfig.setGeneric("true");
                referenceConfig.setVersion(rule.getVersion());
                // 暂时写死
                referenceConfig.setTimeout(5000);
                return referenceConfig;
            });
            GenericService genericService = reference.get();
            Object response = genericService.$invoke(path.getMethodName(), new String[]{path.getParameterType()}, new Object[]{"zhangsan"});
            GatewayResponse gatewayResponse = GatewayResponse.buildGatewayResponse(response);
            ctx.setResponse(gatewayResponse);
        } catch (Throwable t) {
            ctx.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            ctx.setResponse(GatewayResponse.buildGatewayResponse(ResponseCode.INTERNAL_ERROR));
            log.error("complete error", t);
        } finally {
            ctx.written();
            ResponseHelper.writeResponse(ctx);
        }

    }
}
