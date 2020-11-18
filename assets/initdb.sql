/**
dubbo设计，dubbo不同在于需要管理注册中心，管理实体对象，管理方法声明，管理出参、入参；如果直接走invoker的话，我们是可以不考虑出入参声明的。
*/
/**路由管理表，我们默认所有的参数类型和返回值类型都是Object*/
create table connector_dubbo_route (
    id int(18) auto_increment primary key comment '主键',
    route_name varchar(64) comment '规则名称',
    route_code varchar(64) not null comment '规则编码',
    config_center_address varchar(512) not null comment '注册中心地址，目前只支持zookeeper',
    interface_class varchar(256) not null comment '关联接口',
    route_service_port int(5) default 8888 not null comment '路由监听的端口',
    route_service_path varchar(256) not null comment '监听的路径',
    method_name varchar(128) not null comment '关联接口方法',
    parameter_count int(2) comment '参数个数，用于做唯一校验',
    varsion varchar(32) comment '接口版本',
    route_service_in_type int(2) not null default 1 comment '路由入参处理方式，1：表单参数处理，2：json格式处理，3：自定义，暂不支持',
    route_service_out_type int (2) not null default 1 comment '路由出参处理，1：默认返回json格式/字符串',
    is_valiable tinyint(1) default 1 comment '是否启用'
) comment '路由规则配置表';

/**方法参数表*/
create table connector_dubbo_parameter(
    id int(18) auto_increment primary key comment '主键',
    route_id int(18) not null comment '关联方法id',
    parameter_name varchar(128) not null comment '参数名称',
    parameer_type varchar(128) not null comment '参数类型，暂时只支持常规类型',
    format varchar(64) comment '时间类型需要指定格式化',
    sort int(2) comment '排序号',
    del_falg tinyint(1) default 0 comment '是否删除'
) comment '路由接口参数表';
/**后面大概率会增加一张关联实体表，作废了*/
create table connector_dubbo_entity(
    id int(18) auto_increment primary key comment '主键',
    entity_name varchar(128) not null comment '实体名称',
    entity_content text not null comment '实体编码'
) comment '接口关联实体';