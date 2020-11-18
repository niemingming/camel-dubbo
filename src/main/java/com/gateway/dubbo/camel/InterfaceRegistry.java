package com.gateway.dubbo.camel;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口调用注册类
 */
@Data
@Slf4j
public class InterfaceRegistry {

    private static InterfaceRegistry interfaceRegistry;

    private Map<String,InterfaceInfo> interfaceInfos = new ConcurrentHashMap<>();

    private InterfaceRegistry(){
    }

    /**
     * 获取单例信息
     * @return
     */
    public static InterfaceRegistry getInstance() {
        if (interfaceRegistry != null) {
            return interfaceRegistry;
        }
        synchronized (InterfaceRegistry.class.getClass().getName()) {
            if (interfaceRegistry == null) {
                interfaceRegistry = new InterfaceRegistry();
            }
        }
        return interfaceRegistry;
    }

    public InterfaceRegistry registry(String className,InterfaceInfo interfaceInfo) {
        interfaceInfos.put(className, interfaceInfo);
        return this;
    }

    public InterfaceInfo getInterfaceInfo(String className) {
        return interfaceInfos.get(className);
    }
}
