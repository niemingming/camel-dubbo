package com.gateway.dubbo.camel;

public class DubboInvokeException extends Exception {

    public DubboInvokeException(Exception e){
        super(e);
    }
    public DubboInvokeException(String message,Throwable e){
        super(message,e);
    }
}
