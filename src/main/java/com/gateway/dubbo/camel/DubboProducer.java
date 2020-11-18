package com.gateway.dubbo.camel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.dubbo.rpc.proxy.InvokerInvocationHandler;

import java.lang.reflect.Method;

/**
 * dubbo调用者
 */
@Slf4j
public class DubboProducer extends DefaultProducer {

    private InvokerInvocationHandler handler;

    private Method method;

    private String[] parameterNames;

    private String[] parameterTypes;

    public DubboProducer(InvokerInvocationHandler handler, Method method, String[] parameterNames, String[] parameterTypes, Endpoint endpoint) {
        super(endpoint);
        this.handler = handler;
        this.method = method;
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes;
    }

    /**
     * 内部信息为字符串
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getMessage().getBody(String.class);
        Object[] values = new Object[parameterNames.length];
        if (parameterNames.length > 0 && body != null && body.trim().length() > 0) {
            JSONObject obj = JSONObject.parseObject(body);
            for (int i = 0; i < parameterNames.length; i++) {
                if ("java.lang.String".equals(parameterTypes[i])) {
                    values[i] = obj.getString(parameterNames[i]);
                } else if ("java.util.Date".equals(parameterTypes[i])) {
                    values[i] = obj.getDate(parameterNames[i]);
                } else if ("java.lang.Integer".equals(parameterTypes[i]) || "int".equals(parameterTypes[i])
                        ||"java.lang.Long".equals(parameterTypes[i]) || "long".equals(parameterTypes[i])) {
                    values[i] = obj.getLong(parameterNames[i]);
                } else if ("java.lang.Float".equals(parameterTypes[i]) || "float".equals(parameterTypes[i])
                        ||"java.lang.Double".equals(parameterTypes[i]) || "double".equals(parameterTypes[i])) {
                    values[i] = obj.getDouble(parameterNames[i]);
                } else {
                    values[i] = obj.get(parameterNames[i]);
                }
            }
        } else {
            values = null;
        }
        try {
            Object res = handler.invoke(null,method,values);
            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE,"text/plain");
            exchange.getMessage().setBody(JSON.toJSONString(res));
        } catch (Throwable throwable) {
            log.error("调用dubbo服务发生异常！");
            throw new DubboInvokeException("调用后端服务异常：" + throwable.getMessage(),throwable);
        }
    }
}
