package com.gateway.dubbo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * connector_dubbo_route
 * @author nmm
 * @since 2020-11-12
 */
@Data
@TableName("connector_dubbo_route")
public class ConnectorDubboRouteEntity {

    /**
    * 主键
    */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
    * 规则名称
    */
    @NotEmpty(message = "规则名称不能为空！")
    private String routeName;
    /**
    * 规则编码
    */
    @NotEmpty(message = "规则编码不能为空！")
    private String routeCode;
    /**
    * 注册中心地址，目前只支持zookeeper
    */
    @NotEmpty(message = "注册中心地址不能为空！")
    private String configCenterAddress;
    /**
    * 关联接口
    */
    @NotEmpty(message = "关联接口不能为空！")
    private String interfaceClass;
    /**
    * 路由监听的端口
    */
    private Long routeServicePort;
    /**
    * 监听的路径
    */
    @NotEmpty(message = "路由映射路径不能为空！")
    private String routeServicePath;
    /**
    * 关联接口方法
    */
    @NotEmpty(message = "路由规则方法不能为空！")
    private String methodName;
    /**
    * 参数个数，用于做唯一校验
    */
    private Integer parameterCount;
    /**
    * 路由入参处理方式，1：表单参数处理，2：json格式处理，3：自定义，暂不支持
    */
    private Long routeServiceInType = 1l;
    /**
    * 路由出参处理，1：默认返回json格式/字符串
    */
    private Long routeServiceOutType = 1l;
    /**
    * 是否启用
    */
    private Boolean isValiable = true;
    /**
     * 版本
     */
    private String version;
    /**
     * 忽略该属性
     */
    @TableField(exist = false)
    private List<ConnectorDubboParameterEntity> parameters = new ArrayList<>();

}
