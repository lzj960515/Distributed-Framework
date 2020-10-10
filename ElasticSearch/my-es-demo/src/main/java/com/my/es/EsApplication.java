package com.my.es;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@MapperScan(basePackages = {"com.my.es.mapper"})
@SpringBootApplication()
public class EsApplication {
    /**
     * MyBatis Plus 分页插件
     * @return {@link PaginationInterceptor}
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    public static void main(String[] args) {
        SpringApplication.run(EsApplication.class, args);
    }

}
