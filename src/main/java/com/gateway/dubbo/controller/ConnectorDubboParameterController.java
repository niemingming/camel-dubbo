package com.gateway.dubbo.controller;

import com.gateway.dubbo.service.ConnectorDubboParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *  前端控制器
 *
 * @author nmm
 * @since 2020-11-12
 */
@RestController
@RequestMapping("/api/v1/connectordubboparameter")
public class ConnectorDubboParameterController {
    @Autowired
    private ConnectorDubboParameterService connectorDubboParameterService;
}
