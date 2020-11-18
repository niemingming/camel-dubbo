package com.gateway.dubbo.util;

import com.gateway.dubbo.entity.ConnectorDubboParameterEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Data
public class TemplateInfo {

    private String packageName;
    private String className;
    private String interfaceClass;
    private List<MethodInfo> methods = new ArrayList<>();

    public TemplateInfo(String interfaceClass) {
        if (interfaceClass.indexOf(".") > 0) {
            packageName = interfaceClass.substring(0,interfaceClass.lastIndexOf("."));
            className = interfaceClass.substring(interfaceClass.lastIndexOf(".") + 1);
        } else {
            packageName = "null";
            className = interfaceClass;
        }
        this.interfaceClass = interfaceClass;
    }

    /**
     * 添加方法
     * @param methodName
     * @param parameters
     */
    public void addMethod(String methodName, ConnectorDubboParameterEntity... parameters) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setName(methodName);
        if (parameters != null) {
            methodInfo.setParameters(Arrays.asList(parameters));
        }
        methods.add(methodInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateInfo that = (TemplateInfo) o;
        //类名一致，方法个数一致，且方法也都一致。
        return Objects.equals(className, that.className) &&
                methods.size() == that.methods.size()&&
                methods.containsAll(that.methods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methods);
    }
}
