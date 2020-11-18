主要实现方案是采用动态编译+动态调用

核心类：

JdkCompiler，dubbo自带的动态编译类，也可以自己写动态编译
重写ReferenceInvokerCreator 用于创建调用客户端。

说明：

参数类型需要指定，且只支持基本类型

double/int/long/float/boolean/byte/char/Double/Integer/Long/Float/Boolean/Byte/Char/Date/String/List/Map


