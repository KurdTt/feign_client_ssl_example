package com.example.feign.client.status;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = StatusServiceConfiguration.SERVICE_NAME, configuration = StatusServiceConfiguration.class)
public interface ServerStatusApi {

    @GetMapping("/status")
    UUID getTestMessage();

}