package org.imooc.gateway.client.config;

import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.imooc.common.config.ConfigReportEntity;
import org.imooc.common.utils.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: yp
 * @date: 2024/12/2 10:21
 * @description:
 */
public class NacosServerConfigReport implements ServerConfigReport{

    private Logger logger = LoggerFactory.getLogger(NacosServerConfigReport.class);

    private final GatewayProperties properties;
    private final ConfigService configService;

    public NacosServerConfigReport(GatewayProperties gatewayProperties){
        this.properties = gatewayProperties;
        try {
            configService = ConfigFactory.createConfigService(gatewayProperties.getServerAddr());
        } catch (NacosException e) {
            logger.error("NacosServerConfigReport create error:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void publishConfig(ConfigReportEntity configReport) throws NacosException {
        configService.publishConfig(properties.getDataId(), properties.getGroup(), JSONUtil.toJSONString(configReport));
    }
}
