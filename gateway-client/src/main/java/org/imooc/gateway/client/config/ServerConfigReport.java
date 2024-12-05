package org.imooc.gateway.client.config;


import org.imooc.common.config.ConfigReportEntity;

/**
 * @author: yp
 * @date: 2024/12/2 10:01
 * @description:
 */
public interface ServerConfigReport {

    void publishConfig(ConfigReportEntity configReport) throws Exception;

}
