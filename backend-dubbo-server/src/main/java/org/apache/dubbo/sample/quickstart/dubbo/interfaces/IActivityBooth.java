package org.apache.dubbo.sample.quickstart.dubbo.interfaces;


import org.apache.dubbo.sample.quickstart.dubbo.rpc.dto.XReq;

public interface IActivityBooth {

    String sayHi(String str);

    String insert(XReq req);

//    String test(String str, XReq req);

}
