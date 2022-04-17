package com.example.feign.client.info;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoServiceResource {

    private final InfoServiceApi infoServiceApi;

    public InfoServiceResource(InfoServiceApi infoServiceApi) {
        this.infoServiceApi = infoServiceApi;
    }

    @GetMapping("/info")
    public UUID getInfo() {
        return infoServiceApi.getTestMessage();
    }
}