package com.example.feign.client.status;

import com.example.feign.client.common.CustomSSLFactory;
import com.example.feign.client.common.ServiceServerListInstanceSupplier;
import feign.Client;
import feign.Request;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class StatusServiceConfiguration {

    public static final String SERVICE_NAME = "server-status";

    private final StatusServiceServiceConfiguration statusServiceLoadBalancerConfiguration;

    public StatusServiceConfiguration(StatusServiceServiceConfiguration statusServiceLoadBalancerConfiguration) {
        this.statusServiceLoadBalancerConfiguration = statusServiceLoadBalancerConfiguration;
    }


    @Bean
    @Primary
    public Client feignClient(@Qualifier(SERVICE_NAME) Client client,
                              LoadBalancerClient loadBalancerClient,
                              LoadBalancerClientFactory loadBalancerClientFactory) {
        return new FeignBlockingLoadBalancerClient(client, loadBalancerClient, loadBalancerClientFactory);
    }

    @Bean
    @Qualifier(SERVICE_NAME)
    Client sslClient(@Qualifier(SERVICE_NAME) SSLSocketFactory sslSocketFactory) {
        return new Client.Default(sslSocketFactory, new NoopHostnameVerifier());
    }


    @Bean
    @Qualifier(SERVICE_NAME)
    SSLSocketFactory sslSocketFactory() throws Exception {
        char[] password = statusServiceLoadBalancerConfiguration.getTruststore().getPassword();
        String truststorePath = statusServiceLoadBalancerConfiguration.getTruststore().getPath();
        return CustomSSLFactory.create(truststorePath, password);
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
                statusServiceLoadBalancerConfiguration.getReadTimeout(),
                statusServiceLoadBalancerConfiguration.getReadTimeout(),
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