package com.gateway.dubbo.camel;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * dubbo信息存储
 */
@Data
public class InterfaceInfo {
    /**调用的目标类*/
    private Class interfaceClass;
    /**要调用的方法*/
    private Method method;



}
