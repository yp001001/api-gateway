package org.imooc.gateway.client.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * 服务定义
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiService {
    String serviceId() default "";

    String version() default "1.0.0";

    String group() default "default";

    ApiProtocol protocol();

    String patternPath() default "";

    String filtersConfig() default "";

}
