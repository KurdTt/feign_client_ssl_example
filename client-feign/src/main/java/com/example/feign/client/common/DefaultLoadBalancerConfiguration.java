package com.example.feign.client.common;

import com.example.feign.client.info.InfoServiceConfiguration;
import com.example.feign.client.status.StatusServiceConfiguration;
import feign.Request;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@LoadBalancerClients({
        @LoadBalancerClient(name = InfoServiceConfiguration.SERVICE_NAME, configuration = InfoServiceConfiguration.class),
        @LoadBalancerClient(name = StatusServiceConfiguration.SERVICE_NAME, configuration = StatusServiceConfiguration.class)
})
public class DefaultLoadBalancerConfiguration {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    Retryer retryer() {
        return new Retryer.Default(
                1000L,
                1000L,
                3
        );
    }

    @Bean
    @ConditionalOnMissingBean
    Request.Options requestOptions() {
        return new Request.Options(
                1000,
                TimeUnit.MILLISECONDS,
                1000,
                TimeUnit.MILLISECONDS,
                true
        );
    }
}