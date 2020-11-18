package com.gateway.dubbo.util;

import com.gateway.dubbo.entity.ConnectorDubboParameterEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Data
public class MethodInfo {

    private String name;
    private List<ConnectorDubboParameterEntity> parameters = new ArrayList<>();

    /**
     * 获取参数名称集合,这里是排过序的
     * @return
     */
    public String toParameterNames() {
        StringJoiner stringJoiner = new StringJoiner(",","","");
        for (ConnectorDubboParameterEntity parameter : parameters) {
            stringJoiner.add(parameter.getParameterName());
        }
        return stringJoiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        //名称或者参数数量不一致认为是不同的
        return name.equals(that.name) &&
                that.getParameters().size() == parameters.size();
    }

}
