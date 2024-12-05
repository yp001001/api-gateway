package org.imooc.core.context.dubbo.session;

import org.imooc.core.context.GatewayContext;
import org.imooc.core.context.dubbo.IGenericReference;

/**
 * @author: yp
 * @date: 2024/12/4 11:42
 * @description:
 */
public interface GatewaySession {

    IGenericReference getMapper();

    GatewayContext getGatewayContext();

}
