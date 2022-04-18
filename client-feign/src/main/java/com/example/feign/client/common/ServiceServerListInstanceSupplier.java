package com.example.feign.client.common;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

public class ServiceServerListInstanceSupplier implements ServiceInstanceListSupplier {

    private final String serviceId;
    private final ServiceConfiguration serviceConfiguration;

    public ServiceServerListInstanceSupplier(String serviceId, ServiceConfiguration serviceConfiguration) {
        this.serviceId = serviceId;
        this.serviceConfiguration = serviceConfiguration;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return Flux.just(
                Arrays.stream(serviceConfiguration.getPaths().split(","))
                        .map(this::getServerInstance)
                        .collect(Collectors.toList())
        );
    }

    DefaultServiceInstance getServerInstance(String url) {
        String[] parts = url.split(":");
        String host = parts[0].trim();
        int port = Integer.parseInt(parts[1].trim());
        String id = UUID.randomUUID().toString();
        return new DefaultServiceInstance(id, serviceId, host, port, true);
    }
}