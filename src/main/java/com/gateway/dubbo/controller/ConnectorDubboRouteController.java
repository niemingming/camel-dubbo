package com.gateway.dubbo.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gateway.dubbo.entity.ConnectorDubboParameterEntity;
import com.gateway.dubbo.entity.ConnectorDubboRouteEntity;
import com.gateway.dubbo.service.ConnectorDubboParameterService;
import com.gateway.dubbo.service.ConnectorDubboRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *  前端控制器
 *
 * @author nmm
 * @since 2020-11-12
 */
@RestController
@RequestMapping("/api/v1/connectordubboroute")
public class ConnectorDubboRouteController {
    @Autowired
    private ConnectorDubboRouteService connectorDubboRouteService;
    @Autowired
    private ConnectorDubboParameterService parameterService;

    /**
     * 添加路由信息
     * @param routeEntity
     * @return
     */
    @PostMapping("/")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity addRoute(@RequestBody @Validated ConnectorDubboRouteEntity routeEntity) {
        JSONObject res = new JSONObject();
        res.put("success",false);
        routeEntity.setParameterCount(routeEntity.getParameters().size());
        //做唯一性校验
        LambdaQueryWrapper<ConnectorDubboRouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConnectorDubboRouteEntity::getInterfaceClass,routeEntity.getInterfaceClass())
                .eq(ConnectorDubboRouteEntity::getMethodName,routeEntity.getInterfaceClass())
                .eq(ConnectorDubboRouteEntity::getParameterCount,routeEntity.getParameterCount())
                .eq(ConnectorDubboRouteEntity::getIsValiable,false);
        if (connectorDubboRouteService.count(queryWrapper) > 0) {
            res.put("message","接口、方法声明已经存在！");
            return ResponseEntity.ok(res.toJSONString());
        }
        //添加路由
        boolean flag = connectorDubboRouteService.save(routeEntity);
        if (!flag) {
            res.put("message","保存路由信息失败！");
            return ResponseEntity.ok(res.toJSONString());
        }
        //保存参数信息
        if (routeEntity.getParameters().size() > 0) {
            for (ConnectorDubboParameterEntity parameter : routeEntity.getParameters()) {
                parameter.setRouteId(routeEntity.getId());
            }
            parameterService.saveBatch(routeEntity.getParameters());
        }
        res.put("success",true);
        return ResponseEntity.ok(res.toJSONString());
    }

}
