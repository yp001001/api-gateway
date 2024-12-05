package org.imooc.core.filter.limit;

import lombok.extern.slf4j.Slf4j;
import org.imooc.common.enums.ResponseCode;
import org.imooc.common.exception.ResponseException;
import org.imooc.core.context.GatewayContext;
import org.imooc.core.filter.Filter;
import org.imooc.core.filter.FilterAspect;
import org.imooc.core.helper.ResponseHelper;
import org.imooc.core.response.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.imooc.common.constants.FilterConst.*;
import static org.imooc.common.enums.ResponseCode.LIMIT_REQUEST;

/**
 * @author: yp
 * @date: 2024/12/5 16:37
 * @description:限流过滤器
 */
@Slf4j
@FilterAspect(id = LIMIT_FILTER_ID,
        name = LIMIT_FILTER_ID,
        order = LIMIT_FILTER_ORDER)
public class LimitFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(LimitFilter.class);

    // 暂时只使用FixedWindowLimitRequest和统一配置
    private LimitRequest limitRequest = new FixedWindowLimitRequest(100000L, 1);

    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        boolean allow = limitRequest.limit(ctx);
        if(!allow){
            logger.info("{} 请求失败，达到限制阈值", ctx.getRequest().getPath());
            ctx.setResponse(GatewayResponse.buildGatewayResponse(ResponseCode.LIMIT_REQUEST));
            ctx.written();
            ResponseHelper.writeResponse(ctx);
            throw new ResponseException(LIMIT_REQUEST);
        }
    }
}
