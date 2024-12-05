package org.imooc.gateway.client.process;

import java.util.Map;

/**
 * @author: yp
 * @date: 2024/12/3 17:59
 * @description:
 */
public class ParseRuleHandlerFactory {

    private Map<String, ParseRuleHandler> parseRuleHandlerMap;

    public ParseRuleHandlerFactory(Map<String, ParseRuleHandler> parseRuleHandlerMap){
        this.parseRuleHandlerMap = parseRuleHandlerMap;
    }

    public ParseRuleHandler getParseRuleHandler(String protocolType){
        return parseRuleHandlerMap.get(protocolType);
    }

}
