package com.gateway.dubbo.processor;

import com.gateway.dubbo.camel.DubboInvokeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultExchange;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 统一的异常处理类
 */
@Slf4j
@Component
public class ExceptionProcessor implements Processor {
    private String textType = "text/plain";
    @Override
    public void process(Exchange exchange) throws Exception {

        Exception exception = exchange.getException(DubboInvokeException.class);
        if (exception == null) {
            exception = exchange.getException();
        }
        log.error("调用dubbo服务异常：{}",exception.getMessage());
        exception.printStackTrace();
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE,textType);
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE,500);
        exchange.getMessage().setBody("调用dubbo服务异常：" + exception.getMessage());
        //设置异常处理标志
        ((DefaultExchange)exchange).setErrorHandlerHandled(true);
    }
}
