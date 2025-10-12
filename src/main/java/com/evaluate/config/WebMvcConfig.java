package com.evaluate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Web MVC配置
 * 确保HTTP响应使用UTF-8编码，特别是JSON中的中文字符
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 清空默认转换器
        converters.clear();
        
        // 1. 配置String消息转换器使用UTF-8
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        converters.add(stringConverter);
        
        // 2. 配置JSON消息转换器，确保UTF-8编码
        MappingJackson2HttpMessageConverter jsonConverter = jackson2HttpMessageConverter();
        converters.add(jsonConverter);
    }
    
    @Bean
    @Primary
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        
        // 设置ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        // 禁用将日期序列化为时间戳
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 禁用序列化空值
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 注册JavaTimeModule以支持Java 8日期时间类型
        mapper.findAndRegisterModules();
        
        converter.setObjectMapper(mapper);
        
        // 设置默认字符集为UTF-8
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        
        // 设置支持的MediaType，确保使用UTF-8
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
        mediaTypes.add(new MediaType("text", "json", StandardCharsets.UTF_8));
        mediaTypes.add(MediaType.APPLICATION_JSON);
        converter.setSupportedMediaTypes(mediaTypes);
        
        return converter;
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 注册JavaTimeModule以支持Java 8日期时间类型
        mapper.findAndRegisterModules();
        return mapper;
    }
}
