package org.imooc.gateway.client.process;

import org.imooc.common.config.Rule;
import org.imooc.common.exception.ResponseException;
import org.imooc.gateway.client.config.GatewayProperties;
import org.imooc.gateway.client.core.ApiInvoker;
import org.imooc.gateway.client.core.ApiProtocol;
import org.imooc.gateway.client.core.ApiService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.imooc.common.enums.ResponseCode.PARSE_PROTOCOL_ERRPR;

/**
 * @author: yp
 * @date: 2024/12/3 18:19
 * @description:
 */
public class DubboParseRuleHandler implements ParseRuleHandler {

    public final GatewayProperties gatewayProperties;

    public DubboParseRuleHandler(GatewayProperties gatewayProperties){
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public Rule parse(Class<?> beanClass) {
        ApiService apiService = beanClass.getAnnotation(ApiService.class);
        if (!ApiProtocol.DUBBO.equals(apiService.protocol())) {
            throw new ResponseException(PARSE_PROTOCOL_ERRPR);
        }
        Rule rule = new Rule();
        rule.setId(apiService.serviceId());
        rule.setName(apiService.serviceId());
        rule.setProtocol(apiService.protocol().getCode());
        rule.setPrefix(apiService.patternPath());
        rule.setServiceId(gatewayProperties.getDataId());
        rule.setVersion(apiService.version());
        rule.setGroup(apiService.group());
        Class<?>[] interfaces = beanClass.getInterfaces();
        if(null == interfaces || interfaces.length == 0){
            rule.setInterfaceName(beanClass.getName());
        }else{
            rule.setInterfaceName(interfaces[0].getName());
        }
        List<Rule.PathRule> paths = new ArrayList<>();
        Method[] methods = beanClass.getMethods();
        for (Method method : methods) {
            ApiInvoker apiInvoker = method.getAnnotation(ApiInvoker.class);
            if (null != apiInvoker) {
                Rule.PathRule pathRule = new Rule.PathRule();
                String path = apiService.patternPath() + apiInvoker.path();
                pathRule.setPath(path);
                pathRule.setGray(apiInvoker.gray());
                pathRule.setProtocol(ApiProtocol.DUBBO.getCode());
                pathRule.setAuth(apiInvoker.auth());
                pathRule.setMethodName(method.getName());
                pathRule.setLimit(apiInvoker.limit());
                pathRule.setRequestCounts(apiInvoker.requestCounts());
                // TODO:暂时只支持一个参数的泛化
                Class<?>[] parameterTypes = method.getParameterTypes();
                if(null != parameterTypes && parameterTypes.length != 0) {
                    pathRule.setParameterType(parameterTypes[0].getName());
                }
                paths.add(pathRule);
            }
        }
        rule.setPaths(paths);
        return rule;
    }
}
