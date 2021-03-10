package com.zhang.emailsnapshot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
public class FileResolver {

    @Bean
    public CommonsMultipartResolver getFileResolver(){
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver();
        return multipartResolver;
    }
}
