package com.my.rabbitmq.config;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 环境配置
 *
 * @author Zijian Liao
 * @since 1.0.0
 */
@Configuration
public class EnvConfig implements EnvironmentAware {

    @Override
    public void setEnvironment(Environment environment) {
        String property = environment.getProperty("queue-name");
        System.out.println(property);
    }
}
