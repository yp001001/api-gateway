package org.imooc.core.netty.processor;

import com.lmax.disruptor.dsl.ProducerType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.imooc.common.enums.ResponseCode;
import org.imooc.core.Config;
import org.imooc.core.context.HttpRequestWrapper;
import org.imooc.core.disruptor.EventListener;
import org.imooc.core.disruptor.ParallelQueueHandler;
import org.imooc.core.helper.ResponseHelper;

/**
 * Disruptor流程处理类
 */
@Slf4j
public class DisruptorNettyCoreProcessor implements NettyProcessor {

    private static  final String THREAD_NAME_PREFIX = "gateway-queue-";

    private Config config;

    private NettyCoreProcessor nettyCoreProcessor;

    private ParallelQueueHandler<HttpRequestWrapper> parallelQueueHandler;

    public DisruptorNettyCoreProcessor(Config config, NettyCoreProcessor nettyCoreProcessor) {
        this.config = config;
        this.nettyCoreProcessor = nettyCoreProcessor;
        ParallelQueueHandler.Builder<HttpRequestWrapper> builder = new ParallelQueueHandler.Builder<HttpRequestWrapper>()
                .setBufferSize(config.getBufferSize())
                .setThreads(config.getProcessThread())
                .setProducerType(ProducerType.MULTI)
                .setNamePrefix(THREAD_NAME_PREFIX)
                .setWaitStrategy(config.getWaitStrategy());

        BatchEventListenerProcessor batchEventListenerProcessor = new BatchEventListenerProcessor();
        builder.setListener(batchEventListenerProcessor);
        this.parallelQueueHandler = builder.build();

    }

    @Override
    public void process(HttpRequestWrapper wrapper) {
        this.parallelQueueHandler.add(wrapper);
    }



    public class BatchEventListenerProcessor implements EventListener<HttpRequestWrapper>{

        @Override
        public void onEvent(HttpRequestWrapper event) {
            nettyCoreProcessor.process(event);

        }

        @Override
        public void onException(Throwable ex, long sequence, HttpRequestWrapper event) {
            HttpRequest  request = event.getRequest();
            ChannelHandlerContext ctx = event.getCtx();
            try {
                log.error("BatchEventListenerProcessor onException请求写回失败，request:{},errMsg:{} ",request,ex.getMessage(),ex);

                //构建响应对象
                FullHttpResponse fullHttpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
                if(!HttpUtil.isKeepAlive(request)){
                    ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                }else{
                    fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(fullHttpResponse);
                }
            }catch (Exception e){
                log.error("BatchEventListenerProcessor onException请求写回失败，request:{},errMsg:{} ",request,e.getMessage(),e);
            }
        }
    }


    @Override
    public void start() {
        parallelQueueHandler.start();
    }

    @Override
    public void shutDown() {
        parallelQueueHandler.shutDown();
    }



}
