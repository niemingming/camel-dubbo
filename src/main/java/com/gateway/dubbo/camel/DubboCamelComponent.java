package com.gateway.dubbo.camel;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * dubbo转换组件，用于camel的组件注册和声明。
 */
@Slf4j
public class DubboCamelComponent extends DefaultComponent {

    /**
     *
     * @param uri 原始路径
     * @param remaining 新的path
     * @param parameters 所有参数
     * @return
     * @throws Exception
     */
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        List<String> address = new ArrayList<>();
        //注册中心地址需要是zookeeper的，暂时不支持其他
        String centerAddress = "zookeeper://" + remaining;
        Object backup = parameters.remove("backup");
        if (backup != null) {
            centerAddress += "?" + backup.toString();
        }
        address.add(centerAddress);
        //设置配置中心集群信息
        parameters.put("address",address.toArray(new String[address.size()]));
        Object args = parameters.get("parameterNames");
        //设置请求方法信息
        if (args == null) {
            parameters.put("parameterNames",new String[0]);
            parameters.put("parameterTypes",new String[0]);
        } else {
            String names[] = args.toString().split(",");
            parameters.put("parameterNames",names);
            parameters.put("parameterTypes",parameters.get("parameterTypes").toString().split(","));
        }
        //做非空校验
        if (parameters.get("interfaceClass") == null
            ||parameters.get("methodName") == null) {
            log.error("接口类名：{}，方法名：{}，均不可为空！",parameters.get("interfaceClass"),parameters.get("methodName"));
        }

        return new DubboCamelEndpoint(uri,this);
    }
}
