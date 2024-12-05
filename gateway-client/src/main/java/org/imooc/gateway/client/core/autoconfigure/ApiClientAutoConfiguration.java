package org.imooc.gateway.client.core.autoconfigure;

import org.apache.dubbo.config.spring.ServiceBean;
import org.imooc.gateway.client.config.GatewayProperties;
import org.imooc.gateway.client.config.NacosServerConfigReport;
import org.imooc.gateway.client.config.ServerConfigReport;
import org.imooc.gateway.client.config.ServerConfigReportProcessor;
import org.imooc.gateway.client.core.ApiProperties;
import org.imooc.gateway.client.core.ApiProtocol;
import org.imooc.gateway.client.process.DubboParseRuleHandler;
import org.imooc.gateway.client.process.HttpParseRuleHandler;
import org.imooc.gateway.client.process.ParseRuleHandler;
import org.imooc.gateway.client.process.ParseRuleHandlerFactory;
import org.imooc.gateway.client.support.dubbo.Dubbo27ClientRegisterManager;
import org.imooc.gateway.client.support.springmvc.SpringMVCClientRegisterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnProperty(prefix = "api", name = {"registerAddress"})
public class ApiClientAutoConfiguration {

    @Autowired
    private ApiProperties apiProperties;

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMVCClientRegisterManager.class)
    public SpringMVCClientRegisterManager springMVCClientRegisterManager() {
        return new SpringMVCClientRegisterManager(apiProperties);
    }

//    @Bean
    @ConditionalOnClass({ServiceBean.class})
    @ConditionalOnMissingBean(Dubbo27ClientRegisterManager.class)
    public Dubbo27ClientRegisterManager dubbo27ClientRegisterManager() {
        return new Dubbo27ClientRegisterManager(apiProperties);
    }

    @Bean
    @ConditionalOnClass(GatewayProperties.class)
    @ConditionalOnMissingBean(GatewayProperties.class)
    public GatewayProperties gatewayProperties(){
        return new GatewayProperties();
    }

    @Bean
    @ConditionalOnClass(ServerConfigReport.class)
    @ConditionalOnMissingBean(ServerConfigReport.class)
    public ServerConfigReport serverConfigReport(){
        return new NacosServerConfigReport(gatewayProperties());
    }

    @Bean("httpParseRuleHandler")
    public ParseRuleHandler httpParseRuleHandler(Environment environment){
        return new HttpParseRuleHandler(environment);
    }

    @Bean("dubboParseRuleHandler")
    public ParseRuleHandler dubboParseRuleHandler(GatewayProperties gatewayProperties){
        return new DubboParseRuleHandler(gatewayProperties);
    }

    @Bean
    public ParseRuleHandlerFactory parseRuleHandlerFactory(Environment environment){
        Map<String, ParseRuleHandler> parseRuleHandlerMap = new HashMap<>();
        parseRuleHandlerMap.put(ApiProtocol.HTTP.getCode(), httpParseRuleHandler(environment));
        parseRuleHandlerMap.put(ApiProtocol.DUBBO.getCode(), dubboParseRuleHandler(gatewayProperties()));
        return new ParseRuleHandlerFactory(parseRuleHandlerMap);
    }

    @Bean
    @ConditionalOnClass(ServerConfigReportProcessor.class)
    @ConditionalOnMissingBean(ServerConfigReportProcessor.class)
    public ServerConfigReportProcessor serverConfigReportProcessor(ServerConfigReport serverConfigReport,
                                                                   GatewayProperties gatewayProperties,
                                                                   Environment environment){
        return new ServerConfigReportProcessor(serverConfigReport, gatewayProperties, parseRuleHandlerFactory(environment));
    }
}
