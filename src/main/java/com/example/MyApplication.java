package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/***
 * @RequestMapping注解提供路由information.It告诉Spring，任何带有/路径的HTTP请求都应该映射到home方法。
 * @RestController注解告诉Spring将结果字符串直接返回给调用者。
 * @RestController和@RequestMapping注释是Spring MVC注释（它们不特定于Spring Boot）。
 * 有关详细信息，请参阅Spring参考文档中的MVC部分。(https://docs.spring.io/spring-framework/reference/6.2/web/webmvc.html)
 * 第二个类级注释是@SpringBootApplication。此注释称为元注释，
 * 它结合了@SpringBootConfiguration、@EnableAutoConfiguration和@ComponentScan。
 * @EnableAutoConfiguration告诉Spring Boot根据您添加的jar依赖项“猜测”您想如何配置Spring。
 * 由于spring-boot-starter-web添加了Tomcat和Spring MVC，自动配置假定您正在开发Web应用程序并相应地设置Spring。
 */
@RestController
@SpringBootApplication
public class MyApplication {

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
        System.out.println("Hello World!");
    }

}