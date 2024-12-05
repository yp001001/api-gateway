package org.imooc.gateway.client.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 必须要在服务的方法上面强制声明
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInvoker {
    String path();

    boolean gray() default false;

    //TODO: 权限配置校验
    boolean auth() default false;

    String interfaceName() default "";

    String methodName() default "";

    String parameterType() default "";

    boolean limit() default false;

    int requestCounts() default 10;

}
