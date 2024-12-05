package org.imooc.gateway.client.process;

import org.imooc.common.config.Rule;

/**
 * @author: yp
 * @date: 2024/12/3 17:52
 * @description:
 */
public interface ParseRuleHandler {

    Rule parse(Class<?> beanClass);

}
