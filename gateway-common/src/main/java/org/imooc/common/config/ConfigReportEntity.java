package org.imooc.common.config;

import java.util.List;

public class ConfigReportEntity {
    private List<Rule> rules;
    private String serverPrefixName;

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setServerPrefixName(String serverPrefixName) {
        this.serverPrefixName = serverPrefixName;
    }

    public String getServerPrefixName() {
        return serverPrefixName;
    }
}