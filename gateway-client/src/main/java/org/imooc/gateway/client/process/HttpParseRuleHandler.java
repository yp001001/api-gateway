package org.imooc.gateway.client.process;

import org.imooc.common.config.Rule;
import org.imooc.gateway.client.core.ApiInvoker;
import org.imooc.gateway.client.core.ApiProtocol;
import org.imooc.gateway.client.core.ApiService;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: yp
 * @date: 2024/12/3 17:56
 * @description:
 */
public class HttpParseRuleHandler implements ParseRuleHandler {

    private final Environment environment;
    private String serverPrefixPath;

    public HttpParseRuleHandler(Environment environment) {
        this.environment = environment;
        serverPrefixPath = this.environment.getProperty("server.servlet.context-path");

    }

    @Override
    public Rule parse(Class<?> beanClass) {
        Method[] methods = beanClass.getDeclaredMethods();
        ApiService apiService = beanClass.getAnnotation(ApiService.class);
        return parseHttpRule(apiService, methods);

    }

    private Rule parseHttpRule(ApiService apiService, Method[] methods) {
        Rule rule = new Rule();
        rule.setId(apiService.serviceId());
        rule.setName(apiService.serviceId());
        rule.setProtocol(apiService.protocol().getCode());
        rule.setPrefix(apiService.patternPath());
        rule.setServiceId(apiService.serviceId());
        List<Rule.PathRule> paths = new ArrayList<>();
        for (Method method : methods) {
            Rule.PathRule pathRule = new Rule.PathRule();
            ApiInvoker apiInvoker = method.getAnnotation(ApiInvoker.class);
            if (apiInvoker != null) {
                pathRule.setGray(apiInvoker.gray());
                pathRule.setAuth(apiInvoker.auth());
                pathRule.setLimit(apiInvoker.limit());
                pathRule.setRequestCounts(apiInvoker.requestCounts());
                String path = apiInvoker.path();
                if (path.startsWith("/")) {
                    pathRule.setPath(serverPrefixPath + path);
                } else {
                    pathRule.setPath(serverPrefixPath + "/" + path);
                }
                paths.add(pathRule);
            }
        }
        rule.setPaths(paths);
        return rule;
    }

}
