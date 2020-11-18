package com.gateway.dubbo.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * 源码创建器
 */
@Slf4j
@Component
public class SourceCreator implements ApplicationContextAware {

    private Template template;

    public SourceCreator() {
    }

    /**
     * 根据模板生成源码
     * @param dataModal
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public String createSource(TemplateInfo dataModal) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        template.process(dataModal,stringWriter);
        String sourcecode = stringWriter.toString();
        stringWriter.close();
        log.info("源码信息：\n{}" ,sourcecode);
        return sourcecode;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_30);
        try {
            File file = applicationContext.getResource("classpath:template").getFile();
            configuration.setDirectoryForTemplateLoading(file);
            template = configuration.getTemplate("source.tpl");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
