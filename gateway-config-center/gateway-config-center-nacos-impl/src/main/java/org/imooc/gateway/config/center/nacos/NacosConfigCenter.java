package org.imooc.gateway.config.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.imooc.common.config.ConfigServerData;
import org.imooc.common.config.DynamicConfigManager;
import org.imooc.common.config.Rule;
import org.imooc.gateway.config.center.api.ConfigCenter;
import org.imooc.gateway.config.center.api.RulesChangeListener;

import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
public class NacosConfigCenter implements ConfigCenter {
    private static final String DATA_ID = "api-gateway";

    private String serverAddr;

    private String env;

    private ConfigService configService;

    @Override
    public void init(String serverAddr, String env) {
        this.serverAddr = serverAddr;
        this.env = env;

        try {
            configService = NacosFactory.createConfigService(this.serverAddr);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeRulesChange(RulesChangeListener listener) {
        try {
            //初始化通知
            String config = configService.getConfig(DATA_ID, env, 5000);
            //{"rules":[{}, {}]}
            log.info("config from nacos: {}", config);
            if (StringUtils.isNotEmpty(config)) {
                processConfigPublish(config, listener);
            }

            //监听变化
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @SneakyThrows
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("gateway config from nacos: {}", configInfo);
                    processConfigPublish(configInfo, listener);
                }
            });

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }


    private void processConfigPublish(String config, RulesChangeListener listener) throws NacosException {

        List<ConfigServerData> configServerDataList = JSON.parseObject(config, new TypeReference<List<ConfigServerData>>() {
        });

        DynamicConfigManager.getInstance().setConfigServerDatas(configServerDataList);

        for (ConfigServerData configServerData : configServerDataList) {
            String rulesConfig = configService.getConfig(configServerData.getDataId(), configServerData.getGroupId(), 5000);
            List<Rule> rules = JSON.parseObject(rulesConfig).getJSONArray("rules").toJavaList(Rule.class);
            String serverPrefixPath = configServerData.getServerPrefixPath();
            listener.onRulesChange(serverPrefixPath, rules);
            // 监听服务变化
            configService.addListener(configServerData.getDataId(), configServerData.getGroupId(), new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("sever config from nacos: {} {} {}", configInfo, configServerData.getDataId(), configServerData.getGroupId());
                    List<Rule> rules = JSON.parseObject(configInfo).getJSONArray("rules").toJavaList(Rule.class);
                    listener.onRulesChange(serverPrefixPath, rules);
                }
            });
        }
    }

}
