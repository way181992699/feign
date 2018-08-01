package com.ym.feign.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
//此层 相当于 service层的业务接口层
@FeignClient(value = "EUREKA-CLIENT")
public interface SchedualServiceHi {
    @GetMapping("/hi")
    public  String  sayHiFromClientOne(@RequestParam(value = "name") String  name);





}
