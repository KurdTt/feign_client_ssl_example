package com.example.feign.client.info;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = InfoServiceConfiguration.SERVICE_NAME, configuration = InfoServiceConfiguration.class)
public interface InfoServiceApi {

    @GetMapping("/test")
    UUID getTestMessage();

}