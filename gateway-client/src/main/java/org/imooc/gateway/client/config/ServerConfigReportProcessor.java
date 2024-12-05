package org.imooc.gateway.client.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.imooc.common.config.ConfigReportEntity;
import org.imooc.common.config.Rule;
import org.imooc.common.enums.ProtocolType;
import org.imooc.common.exception.ResponseException;
import org.imooc.common.utils.JSONUtil;
import org.imooc.gateway.client.core.ApiInvoker;
import org.imooc.gateway.client.core.ApiProtocol;
import org.imooc.gateway.client.core.ApiService;
import org.imooc.gateway.client.process.ParseRuleHandler;
import org.imooc.gateway.client.process.ParseRuleHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.*;

import static org.imooc.common.enums.ResponseCode.PARSE_PROTOCOL_ERRPR;

/**
 * @author: yp
 * @date: 2024/12/2 10:56
 * @description:
 */
public class ServerConfigReportProcessor implements ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(ServerConfigReportProcessor.class);

    private final ServerConfigReport serverConfigReport;

    private final GatewayProperties gatewayProperties;


    private ApplicationContext applicationContext;

    private ParseRuleHandlerFactory parseRuleHandlerFactory;


    public ServerConfigReportProcessor(ServerConfigReport serverConfigReport,
                                       GatewayProperties gatewayProperties,
                                       ParseRuleHandlerFactory parseRuleHandlerFactory) {
        this.serverConfigReport = serverConfigReport;
        this.gatewayProperties = gatewayProperties;
        this.parseRuleHandlerFactory = parseRuleHandlerFactory;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        ConfigReportEntity configReportEntity = new ConfigReportEntity();
        configReportEntity.setServerPrefixName(gatewayProperties.getPrefixPath());

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        List<Rule> rules = new ArrayList<>();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            if (AnnotationUtils.findAnnotation(beanClass, ApiService.class) != null) {
                ApiService apiService = beanClass.getAnnotation(ApiService.class);
                ParseRuleHandler parseRuleHandler = parseRuleHandlerFactory.getParseRuleHandler(apiService.protocol().getCode());
                if(null == parseRuleHandler){
                    throw new ResponseException(PARSE_PROTOCOL_ERRPR);
                }
                rules.add(parseRuleHandler.parse(beanClass));
            }
        }
        configReportEntity.setRules(rules);
        logger.info("读取服务：{} 中的所有配置rule信息：{}", gatewayProperties.getPrefixPath(), JSONUtil.toJSONString(configReportEntity));
        serverConfigReport.publishConfig(configReportEntity);
    }


}
