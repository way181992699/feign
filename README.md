# feign
SpringCloud -feign （RPC） 服务与服务之间的远程调用 集成了ribbon（负载均衡策略）
1.关键字

  负载均衡，httClient, RPC

2.基本介绍

Feign 是一个声明式web服务客户端. 它使得编写web服务非常简单.对程序员来说，只需要编写一个接口，并使用Feign提供的注解即可。Feign支持如：Feign 和 JAX-RS 注解. Feign 也支持可插拔式 编码、解码. Spring Cloud 对feign进行了二次封装，使其支质变Spring MVC 注解和HttpMessageConverters，以及集成了Ribbon 和 Eureka 以支持客户端负载均衡。



前一节我们介绍了spring cloud eureka，也创建了一个简单的实例。对注册发现体系而言，eureka只是注册发现的的注册中心，需要微服务“提供方”进行注册，同时微服务“调用方”发现并使用提供方提供的微服务接口才有意义。提供方如何注册自己；又如何通过注册中心发现服务提供方，并负载均衡；远程调用失败重试以及如何让远程调用如本地接口调用一样简单方便都是微服务客户端面临的挑战。spring cloud提供的feign就是为了解决这个问题。为了更简单的理解，feign可以认为是远程调用中的httpClient。



3.创建可调用的微服务

整个微服务体系，至少需要一个微服务提供方，一个服务注册中心，和一个服务调用方。下面我们将搭建一个微服务项目，并提供一个通过用户名返回对应用户详情的接口，同时创建一个调用服务提供方接口的项目，并返回用户详情的json数据。



3.1创建微服务－服务提供方－接口项目

创建一个名为:mc-client-provider-spi的项目，结构如下：



maven pom.xml配置如下：

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.3.RELEASE</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
 
    <groupId>com.mico.sharp</groupId>
    <artifactId>mc-client-provider-spi</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
 
    <name>mc-client-provider-spi</name>
    <url>http://maven.apache.org</url>
 
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
 
    <!--依赖-->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Brixton.SR5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
 
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>


创建服务接口－model
package com.mico.sharp.client.provider.spi.model;
 
import java.io.Serializable;
 
/**
 * Created  on 17/8/15.
 */
public class User implements Serializable{
    private String name;
 
    private Integer age;
 
    private String gender;
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public Integer getAge() {
        return age;
    }
 
    public void setAge(Integer age) {
        this.age = age;
    }
 
    public String getGender() {
        return gender;
    }
 
    public void setGender(String gender) {
        this.gender = gender;
    }
}


创建接口

package com.mico.sharp.client.provider.spi;
 
import com.mico.sharp.client.provider.spi.model.User;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
 
/**
 * Created on 17/6/29.
 */
@FeignClient(value = "mc-client-provider")  //  [A]
public interface UserSpi {
 
    @RequestMapping(value = "/api/v1/user/{userName}",method = RequestMethod.GET)
    User findUserByName(@PathVariable(name = "userName", required = true) String userName);
}


通过maven打包：mc-client-provider-spi-1.0.0.jar,并推送到maven仓库（如果没有公共仓库可推送到本地仓库）



3.2创建微服务－服务提供方

创建一个名为:mc-client-provider的项目，结构如下：



maven pom.xml配置如下：

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.3.RELEASE</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
 
    <groupId>com.mico.sharp</groupId>
    <artifactId>mc-client-provider</artifactId>
    <packaging>jar</packaging>
 
    <name>mc-client-provider</name>
    <url>http://maven.apache.org</url>
 
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
 
    <!--依赖-->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Brixton.SR5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
 
    <dependencies>
        <!--spring cloud eureka client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <!--feign-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
        <!--actuator 用于打开默认的endpoints:如：Status Page and Health Indicator-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency><!--引用接口项目-->
            <groupId>com.mico.sharp</groupId>
            <artifactId>mc-client-provider-spi</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
 
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>


创建main class
package com.mico.sharp.client.provider;
 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
 
@SpringBootApplication
@EnableEurekaClient//eureka client
@EnableFeignClients
public class App
{
    public static void main( String[] args )
    {
        //启动项目
        SpringApplication.run(App.class,args);
    }
}


创建 服务接口
package com.mico.sharp.client.provider.controller;
 
import com.mico.sharp.client.provider.spi.UserSpi;
import com.mico.sharp.client.provider.spi.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
 
/**
 * Created on 17/8/15.
 */
@RestController//必须配置，以生成对应bean
public class UserController implements UserSpi {
    @Value("${server.port}")
    Integer port; //获取当前应用端口，以测试负载均衡
 
    @Override
    public User findUserByName(@PathVariable(name = "userName", required = true) String userName) {
        User user = new User();
        user.setName(userName);
        user.setAge(port);
        user.setGender("男");
        return user;
    }
}

创建配置文件：classpath 下创建application.yml
#应用名称
spring:
  application:
    name: mc-client-provider
 
#端口
server:
  port: 7171
 
#注册发现配置：配置注册中心地址及用户名密码
eureka:
  instance:
    preferIpAddress: true
  client:
    serviceUrl:
      defaultZone: http://user:user123@localhost:8181/eureka/
    healthcheck:
      enabled: true
 
# 安全认证的配置：通过用户名和密码访问注册中心
security:
  basic:
    enabled: true
  user:
    name: user  # 用户名
    password: user123   # 用户密码


启动上一节创建的注册中心；
分别以7272和7171两个端口启动当前provider，生成两个实例；

在浏览器中打开http:localhost:8080/查看，微服务提供方是否注册成功如下：



3.2创建微服务调用方

创建名为：mc-clieng-caller的项目，结构如下：



maven pom配置如下：

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.3.RELEASE</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>
 
    <groupId>com.mico.sharp</groupId>
    <artifactId>mc-client-caller</artifactId>
    <packaging>jar</packaging>
 
    <name>mc-client-caller</name>
    <url>http://maven.apache.org</url>
 
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
 
    <!--依赖-->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Brixton.SR5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
 
    <dependencies>
        <!--spring cloud eureka client-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <!--actuator 用于打开默信的endpoints:如：Status Page and Health Indicator-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!--feign-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
        <!--spi-->
        <dependency>
            <groupId>com.mico.sharp</groupId>
            <artifactId>mc-client-provider-spi</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
 
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>

创建main class
package com.mico.sharp.client.caller;
 
import com.mico.sharp.client.provider.spi.UserSpi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
 
@SpringBootApplication
//@SpringBootConfiguration
//@EnableAutoConfiguration
@EnableEurekaClient//eureka client
@EnableFeignClients(basePackageClasses = {UserSpi.class})//同时扫描spi
//@ComponentScan("com.mico.sharp")//同时扫描spi
//@Import(UserSpi.class)
public class App
{
    public static void main( String[] args )
    {
        //启动项目
        SpringApplication.run(App.class,args);
    }
}


创建服务调用controller
package com.mico.sharp.client.caller.controller;
 
import com.mico.sharp.client.provider.spi.UserSpi;
import com.mico.sharp.client.provider.spi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
 
import java.util.HashMap;
import java.util.Map;
 
/**
 * Created on 17/6/30.
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserSpi userSpi;
 
    @RequestMapping("/{userName}")
    @ResponseBody
    public Map<String, Object> sayHello(@PathVariable String userName) {
        try {
            User user = userSpi.findUserByName(userName);
            Map<String,Object> rs = new HashMap<String, Object>();
            rs.put("user",user);
            rs.put("msg","hello ," + userName);
            return rs;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}

创建配置文件：application.yml
#应用名称
spring:
  application:
    name: mc-client-caller
 
#端口
server:
  port: 9090
 
#自身配置：关闭客户端身份
eureka:
  instance:
    preferIpAddress: true
    statusPageUrlPath: ${management.context-path}/info
    healthCheckUrlPath: ${management.context-path}/health
    instance-id: ${spring.cloud.client.ipAddress}:${server.port}
    homePageUrl: https://${spring.cloud.client.ipAddress}:${server.port}
  client:
    #registerWithEureka: false
    #fetchRegistry: false
    serviceUrl:
      defaultZone: http://user:user123@localhost:8181/eureka/
    healthcheck:
      enabled: true
 
 
 
# 安全认证的配置
security:
  basic:
    enabled: true
  user:
    name: user  # 用户名
    password: user123   # 用户密码


启动应用


在浏览器中打开：http://localhost:9090/user/wangDaChui



age是取自微服务提供方的端口，我们上面启动了7171和7272两个端口对应的两个实例。上面显示负载均衡选中的是7171

再刷新一次，结果如下：



由于 feign中引用了ribbon（实现了客户端负载均衡），而ribbon默认是轮循，因此多次刷新请求时，会交叉负载到不同的服务提供方。



3.3基本流程

mc-client-provider 启动，生成UserController bean
mc-client-provider 通过eureka client及eureka.client.serviceUrl.defaultZone配置的注册中心地址，注册自己的ip和端口以及名称：mc-client-provider
mc-client-caller 启动，生成UserController bean
通过http://localhost:9090/user/wangDaChui 调用mc-client-caller中的userContrller
mc-client-caller中userController中调用UserSpi，userSpi代理，先通过eureka client从配置中心获取名为mc-client-provider的ip和端口列表。
mc-client-caller中feign委托ribbon选出一个可用的ip和端口，并发送http请求（http://providerIP:providerPort/api/vi/user/wangDaChui），调用mc-client-provider中的返回给mc-clieng-caller中的userController
