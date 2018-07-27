package org.tron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by liangzhiyan
 */
@Configuration//配置控制
@EnableAutoConfiguration
@ComponentScan//组件扫描
@ImportResource({"classpath:applicationContext.xml"})
public class Start {
    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }
}
