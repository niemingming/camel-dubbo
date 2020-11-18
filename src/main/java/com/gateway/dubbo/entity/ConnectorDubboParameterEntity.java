package com.gateway.dubbo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * connector_dubbo_parameter
 * @author nmm
 * @since 2020-11-12
 */
@Data
@TableName("connector_dubbo_parameter")
public class ConnectorDubboParameterEntity {

    /**
    * 主键
    */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
    * 关联方法id
    */
    private Long routeId;
    /**
    * 参数名称
    */
    @NotEmpty(message = "参数声明不能为空！")
    private String parameterName;
    /**
     * 参数类型
     */
    @NotEmpty(message = "参数类型不能为空！")
    private String parameterType;
    /**
     * 时间类型格式化
     */
    private String format;
    /**
    * 排序号
    */
    private Long sort;
    /**
    * 是否删除
    */
    private Boolean delFalg = false;

}
