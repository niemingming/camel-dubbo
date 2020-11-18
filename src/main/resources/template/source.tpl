<#if packageName != "null">package ${packageName};</#if>
public interface ${className}{
    <#list methods as method>
    public Object ${method.name}(<#list method.parameters as parameter>${parameter.parameterType} ${parameter.parameterName} <#sep>,</#list>);
    </#list>
}