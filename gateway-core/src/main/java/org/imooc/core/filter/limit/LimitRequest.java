package org.imooc.core.filter.limit;

import org.imooc.common.config.Rule;
import org.imooc.core.context.GatewayContext;

/**
 * @author: yp
 * @date: 2024/12/5 16:44
 * @description:
 */
public interface LimitRequest {

    boolean limit(GatewayContext context);

}
