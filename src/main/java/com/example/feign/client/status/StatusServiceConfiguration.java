package com.example.feign.client.status;

import com.example.feign.client.common.ServiceServerListInstanceSupplier;
import feign.Request;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;

public class StatusServiceConfiguration {

    public static final String SERVICE_NAME = "server-status";

    private final StatusServiceServiceConfiguration statusServiceLoadBalancerConfiguration;

    public StatusServiceConfiguration(StatusServiceServiceConfiguration statusServiceLoadBalancerConfiguration) {
        this.statusServiceLoadBalancerConfiguration = statusServiceLoadBalancerConfiguration;
    }

    /**
     * Przykładowe nadpisanie algorytmu dla konkretnego load balancera.
     * W tym przypadku mamy random. Domyślnie jest round robin
     */
    @Bean
    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(LoadBalancerClientFactory loadBalancerClientFactory) {
        return new RandomLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(SERVICE_NAME, ServiceInstanceListSupplier.class),
                SERVICE_NAME
        );
    }

    @Bean
    ServiceInstanceListSupplier serviceStatusInstanceListSupplier() {
        return new ServiceServerListInstanceSupplier(SERVICE_NAME, statusServiceLoadBalancerConfiguration);
    }

    @Bean
    Retryer retryer() {
        return new Retryer.Default(
                1000L,
                1000L,
                statusServiceLoadBalancerConfiguration.getRetries()
        );
    }

    @Bean
    Request.Options requestOptions() {
        return new Request.Options(
                statusServiceLoadBalancerConfiguration.getConnectionTimeout(),
                TimeUnit.MILLISECONDS,
                statusServiceLoadBalancerConfiguration.getReadTimeout(),
                TimeUnit.MILLISECONDS,
                true
        );
    }
}