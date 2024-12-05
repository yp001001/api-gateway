package org.apache.dubbo.sample.quickstart.dubbo.rpc;


import com.alibaba.fastjson.JSON;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.sample.quickstart.dubbo.interfaces.IActivityBooth;
import org.apache.dubbo.sample.quickstart.dubbo.rpc.dto.XReq;
import org.imooc.gateway.client.core.ApiInvoker;
import org.imooc.gateway.client.core.ApiProtocol;
import org.imooc.gateway.client.core.ApiService;

@Service(version = "1.0.0")
@ApiService(protocol = ApiProtocol.DUBBO, patternPath = "/dubbo", version = "1.0.0", group = "default")
public class ActivityBooth implements IActivityBooth {

    @ApiInvoker(path = "/abc")
    @Override
    public String sayHi(String str) {
        return "hi " + str + " by api-gateway-test-provider";
    }

    @ApiInvoker(path = "/bcd")
    @Override
    public String insert(XReq req) {
        return "hi " + JSON.toJSONString(req) + " by api-gateway-test-provider";
    }

//    @ApiInvoker(path = "/def")
//    @Override
//    public String test(String str, XReq req) {
//        return "hi " + str + JSON.toJSONString(req) + " by api-gateway-test-provider";
//    }

}
