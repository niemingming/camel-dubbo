package com.gateway.dubbo.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gateway.dubbo.camel.DubboCamelComponent;
import com.gateway.dubbo.camel.InterfaceInfo;
import com.gateway.dubbo.camel.InterfaceRegistry;
import com.gateway.dubbo.entity.ConnectorDubboParameterEntity;
import com.gateway.dubbo.entity.ConnectorDubboRouteEntity;
import com.gateway.dubbo.processor.DefaultInProcessor;
import com.gateway.dubbo.processor.ExceptionProcessor;
import com.gateway.dubbo.service.ConnectorDubboParameterService;
import com.gateway.dubbo.service.ConnectorDubboRouteService;
import com.gateway.dubbo.util.SourceCreator;
import com.gateway.dubbo.util.TemplateInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.dubbo.common.compiler.support.JdkCompiler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * camel容器配置信息
 */
@Data
@Slf4j
@Component
@EnableScheduling
public class RouteContainer implements InitializingBean {
    @Autowired
    private CamelContext camelContext;

    private InterfaceRegistry registry = InterfaceRegistry.getInstance();
    @Autowired
    private ConnectorDubboRouteService routeService;
    @Autowired
    private ConnectorDubboParameterService parameterService;
    //缓存类信息
    private Map<String,TemplateInfo> cache = new ConcurrentHashMap<>();
    @Autowired
    private DefaultInProcessor inProcessor;
    @Autowired
    private ExceptionProcessor exceptionProcessor;
    @Autowired
    private DubboServiceConfig dubboServiceConfig;
    @Autowired
    private SourceCreator sourceCreator;

    private Map<String,Integer> retries = new HashMap<>();

    private final static String dubboSchema = "dubbo://";

    @Override
    public void afterPropertiesSet() throws Exception {
        //注册新的主键
        camelContext.addComponent("dubbo",new DubboCamelComponent());
    }

    /**
     * 定时刷新
     */
    @Scheduled(fixedDelayString = "${camel.refreshPeriod}")
    public void refresh(){
        log.info("同步路由信息开始！");
        //加载方法信息并分组
        LambdaQueryWrapper<ConnectorDubboParameterEntity> parameterQueryWrapper = new LambdaQueryWrapper<>();
        parameterQueryWrapper.select(ConnectorDubboParameterEntity::getRouteId,ConnectorDubboParameterEntity::getParameterName,ConnectorDubboParameterEntity::getParameterType)
                .eq(ConnectorDubboParameterEntity::getDelFalg,false).orderByAsc(ConnectorDubboParameterEntity::getRouteId, ConnectorDubboParameterEntity::getSort);
        Map<Long,List<ConnectorDubboParameterEntity>> paramters =parameterService.list(parameterQueryWrapper).stream()
                .collect(Collectors.toMap(ConnectorDubboParameterEntity::getRouteId, p -> {
                    List<ConnectorDubboParameterEntity> names = new ArrayList<>();
                    names.add(p);
                    return names;
                },(a,b)-> {a.addAll(b);return a;}));
        //加载路由信息
        LambdaQueryWrapper<ConnectorDubboRouteEntity> routeQueryWrapper = new LambdaQueryWrapper<>();
        routeQueryWrapper.select(ConnectorDubboRouteEntity::getId,ConnectorDubboRouteEntity::getInterfaceClass,ConnectorDubboRouteEntity::getConfigCenterAddress,
                ConnectorDubboRouteEntity::getMethodName,ConnectorDubboRouteEntity::getRouteServicePath,ConnectorDubboRouteEntity::getRouteServicePort,
                ConnectorDubboRouteEntity::getVersion).eq(ConnectorDubboRouteEntity::getIsValiable,true);
        List<ConnectorDubboRouteEntity> routeEntities = routeService.list(routeQueryWrapper);
        //删除信息
        checkRemoveRoutes(routeEntities);
        //编译信息
        Map<String,TemplateInfo> sourceInfo = routeEntities.stream().collect(Collectors.toMap(ConnectorDubboRouteEntity::getInterfaceClass,r -> {
            TemplateInfo templateInfo = new TemplateInfo(r.getInterfaceClass());
            List<ConnectorDubboParameterEntity> names = paramters.get(r.getId());
            if (names == null) {
                names = new ArrayList<>();
            }
            templateInfo.addMethod(r.getMethodName(),names.toArray(new ConnectorDubboParameterEntity[names.size()]));
            return templateInfo;
        }, (r1,r2) -> {
            r1.getMethods().addAll(r2.getMethods());
            return r1;
        }));
        //做对比，来判断哪些需要编译
        checkComplierRoute(sourceInfo);
        //重新编译后，判断新增接口
        List<String> routeIds = camelContext.getRoutes().stream()
                .map(Route::getRouteId).collect(Collectors.toList());
        List<ConnectorDubboRouteEntity> addRoutes = routeEntities.stream().filter(r -> {
            //没有添加进去的需要标识出来,重试次数超过5次不在执行
            return !routeIds.contains(dubboServiceConfig.getRouteIdPrefx() + r.getId())
                    && retries.getOrDefault(dubboServiceConfig.getRouteIdPrefx() + r.getId(),0)<= 5;
        }).collect(Collectors.toList());

        addRoutes(addRoutes,paramters);
        log.info("同步路由信息结束！");
    }

    /**
     * 判断是否需要重新编译
     * @param sourceInfo
     */
    private void checkComplierRoute(Map<String, TemplateInfo> sourceInfo) {
        log.info("校验重新编译类信息!");
        List<TemplateInfo> templateInfos = sourceInfo.values().stream().filter( t -> {
            TemplateInfo classInfo = cache.get(t.getInterfaceClass());
            if (classInfo == null) {
                return true;
            }
            //如果不不同表示需要编译
            return !classInfo.equals(t);
        }).collect(Collectors.toList());
        //防止重复类，不能重新加载。
        JdkCompiler jdkCompiler = new JdkCompiler();
        for (TemplateInfo templateInfo : templateInfos) {
            log.info("重新编译类：{}",templateInfo.getClassName());
            try{
                Class clazz = jdkCompiler.doCompile(templateInfo.getInterfaceClass(),sourceCreator.createSource(templateInfo));
                //动态编译类的所有方法都需要注册
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    InterfaceInfo interfaceInfo = new InterfaceInfo();
                    interfaceInfo.setInterfaceClass(clazz);
                    interfaceInfo.setMethod(method);
                    //key值为类名:方法名:参数个数
                    String key = templateInfo.getInterfaceClass() + ":" + method.getName() + ":" + method.getParameterCount();
                    registry.registry(key,interfaceInfo);
                }
                //编译通过，缓存起来
                cache.put(templateInfo.getInterfaceClass(),templateInfo);
            }catch (Throwable t) {
                log.error("编译类：{}出错！",templateInfo.getInterfaceClass());
                t.printStackTrace();
            }
        }
    }

    /**
     * 校验是否有需要删除的路由信息
     * 在部署，但是数据库中已经没有了
     * @param routeEntities
     */
    private void checkRemoveRoutes(List<ConnectorDubboRouteEntity> routeEntities) {
        log.info("校验删除路由信息！");
        List<String> routeIds = routeEntities.stream().map(r -> dubboServiceConfig.getRouteIdPrefx() + r.getId())
                .collect(Collectors.toList());
        //没有包含在记录中的数据都需要删除
        List<Route> routes = camelContext.getRoutes().stream().filter(r -> !routeIds.contains(r.getRouteId())).collect(Collectors.toList());
        for (Route route : routes) {
            try {
                removeRoutes(route.getRouteId());
            } catch (Exception e) {
                log.error("删除路由{}出现异常！",route.getRouteId());
                e.printStackTrace();
            }
        }
    }

    /**
     * 尝试删除路由
     * @param routeId
     * @throws Exception
     */
    private void removeRoutes(String routeId) throws Exception {
        log.info("尝试删除路由：{}",routeId);
        if (camelContext.getRoute(routeId) == null) {
            log.info("路由未启动！");
            return;
        }
        //尝试停止路由，并删除
        camelContext.getRouteController().stopRoute(routeId);
        if (camelContext.removeRoute(routeId)) {
            retries.remove(routeId);
            return;
        }
        throw new RuntimeException("删除路由失败");
    }

    /**
     * 添加路由信息,
     * dubbo格式：
     * dubbo://configcenteraddress:port?backup=address:port,address:port&version=&interfaceClass&parameterNames=
     */
    private void addRoutes(List<ConnectorDubboRouteEntity> classInfos, Map<Long, List<ConnectorDubboParameterEntity>> paramters) {
        //需要编译的话在之前编译，且注册到registry中。
        log.info("添加路由信息开始！");

        RouteBuilder r = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                //异常处理
                errorHandler(defaultErrorHandler().onExceptionOccurred(exceptionProcessor));
                for (ConnectorDubboRouteEntity routeEntity : classInfos) {
                    String routeId = dubboServiceConfig.getRouteIdPrefx() + routeEntity.getId();
                    log.info("尝试添加路由：{}",routeId);
                    //监听地址
                    String linstenerId = dubboServiceConfig.getListenerAddress() + ":" + routeEntity.getRouteServicePort() + routeEntity.getRouteServicePath();
                    log.debug("路由监听地址：{}",linstenerId);
                    //调用服务地址
                    StringBuilder toaddress = new StringBuilder(dubboSchema);
                    toaddress.append(routeEntity.getConfigCenterAddress());
                    if (!toaddress.toString().contains("?")) {
                        toaddress.append("?");
                    } else {
                        toaddress.append("&");
                    }
                    toaddress.append("interfaceClass=").append(routeEntity.getInterfaceClass())
                        .append("&methodName=").append(routeEntity.getMethodName());
                    if (!StringUtils.isEmpty(routeEntity.getVersion())) {
                        toaddress.append("&version=").append(routeEntity.getVersion());
                    }
                    if(paramters.get(routeEntity.getId()) != null && paramters.get(routeEntity.getId()).size() > 0 ) {
                        String parameterNames = paramters.get(routeEntity.getId()).stream().map(ConnectorDubboParameterEntity::getParameterName).collect(Collectors.joining(","));
                        toaddress.append("&parameterNames=").append(parameterNames);
                        String parameterTypes = paramters.get(routeEntity.getId()).stream().map(ConnectorDubboParameterEntity::getParameterType).collect(Collectors.joining(","));
                        toaddress.append("&parameterTypes=").append(parameterTypes);
                    }
                    log.debug("路由服务地址：{}",toaddress.toString());
                    //添加路由
                    from(linstenerId)
                            .process(inProcessor)
                            .routeId(routeId)
                            .to(toaddress.toString());
                    //设置重试次数
                    retries.put(routeId, retries.getOrDefault(routeId,-1) + 1);
                }
            }
        };

        try {
            camelContext.addRoutes(r);
        } catch (Exception e) {
            log.error("");
            e.printStackTrace();
        }
        log.info("添加路由信息结束！");
    }

}
