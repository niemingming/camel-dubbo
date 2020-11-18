package com.gateway.dubbo.camel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;

import java.util.Optional;

@Data
@Slf4j
@UriEndpoint(
        scheme = "dubbo",
        syntax = "dubbo:registryAddress:port", //dubbo://localhost:2181?version=1.0.0&interfaceClass=com.test.HelloService
        producerOnly = true, // 仅仅作为提供者，不需要作为消费者
        title = "dubbo component endpoint"
)
public class DubboCamelEndpoint extends DefaultEndpoint {

    @UriPath(
            name = "registryAddress",
            description = "注册中心地址"
    )
    private String[] address;

    @UriParam(
            name = "interfaceClass",
            description = "dubbo接口类名"
    )
    @Metadata(required = true)
    private String interfaceClass;
    @UriParam(name = "version",description = "接口版本")
    private String version;
    @UriParam(name = "methodName",description = "接口方法名")
    private String methodName;


    @UriParam(name = "parameterNames",description = "接口参数key值")
    private String[] parameterNames;
    @UriParam(name = "parameterTypes",description = "接口参数类型")
    private String[] parameterTypes;


    public DubboCamelEndpoint(String url, Component component) {
        super(url,component);
    }

    @Override
    public Producer createProducer() throws Exception {
        String key = interfaceClass + ":" + methodName + ":" + parameterNames.length;
        InterfaceInfo interfaceInfo = InterfaceRegistry.getInstance().getInterfaceInfo(key);
        if (!Optional.ofNullable(interfaceInfo).isPresent()) {
            throw new RuntimeException("未注册接口调用类型！");
        }
        ReferenceInvokerCreator referenceInvokerCreator = new ReferenceInvokerCreator(interfaceInfo.getInterfaceClass(),address);
        if (version != null) {
            referenceInvokerCreator.setVersion(version);
        }

        return new DubboProducer(referenceInvokerCreator.getInvokerHandler(),interfaceInfo.getMethod(),parameterNames,parameterTypes,this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("不支持消费者！");
    }
}
