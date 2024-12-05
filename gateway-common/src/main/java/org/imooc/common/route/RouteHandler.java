package org.imooc.common.route;

import org.imooc.common.config.Rule;

/**
 * @author: yp
 * @date: 2024/12/5 10:09
 * @description:
 */
@FunctionalInterface
public interface RouteHandler<T, R> {

    R apply(T input);

}
