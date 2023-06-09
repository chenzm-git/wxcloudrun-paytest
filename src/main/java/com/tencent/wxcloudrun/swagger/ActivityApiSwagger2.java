package com.tencent.wxcloudrun.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author:blueskyevil
 * @Date:2019/3/21
 */
@Configuration
@EnableSwagger2
public class ActivityApiSwagger2
{
    @Bean
    public Docket atomicPowerRestApi()
    {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).groupName("chart-service").select()
                .apis(RequestHandlerSelectors.basePackage("com.tencent.wxcloudrun"))
                .paths(PathSelectors.any()).build();
    }

    /**
     * api地址
     *
     * @return
     */
    private ApiInfo apiInfo()
    {
        return new ApiInfoBuilder().title("Spring Boot - Swagger Api File").description("测试接口")
                .termsOfServiceUrl("http://ip:port/border-test").version("1.0").build();
    }
}
