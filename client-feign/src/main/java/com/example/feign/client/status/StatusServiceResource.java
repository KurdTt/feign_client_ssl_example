package com.example.feign.client.status;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusServiceResource {

    private final ServerStatusApi serverStatusApi;

    public StatusServiceResource(ServerStatusApi serverStatusApi) {
        this.serverStatusApi = serverStatusApi;
    }

    @GetMapping("/status")
    public UUID getStatus() {
        return serverStatusApi.getTestMessage();
    }
}