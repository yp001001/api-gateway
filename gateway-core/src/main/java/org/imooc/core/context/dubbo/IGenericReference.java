package org.imooc.core.context.dubbo;

import java.util.Map;

/**
 * @author: yp
 * @date: 2024/12/4 11:29
 * @description:统一泛化调用接口
 */
public interface IGenericReference {

    Object $invoke(Map<String, Object> params);

}
