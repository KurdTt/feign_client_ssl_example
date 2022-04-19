package com.example.feign.client.info;

import com.example.feign.client.common.ServiceServerListInstanceSupplier;
import feign.Client;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import feign.httpclient.ApacheHttpClient;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ResourceUtils;

public class InfoServiceConfiguration {

    public static final String SERVICE_NAME = "server-info";

    private final InfoServiceServiceConfiguration infoServiceLoadBalancerConfiguration;

    public InfoServiceConfiguration(InfoServiceServiceConfiguration infoServiceLoadBalancerConfiguration) {
        this.infoServiceLoadBalancerConfiguration = infoServiceLoadBalancerConfiguration;
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
    Client sslClient() throws Exception {
        if (infoServiceLoadBalancerConfiguration.isSecured()) {
            char[] password = infoServiceLoadBalancerConfiguration.getTruststore().getPassword();
            String truststorePath = infoServiceLoadBalancerConfiguration.getTruststore().getPath();
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(ResourceUtils.getFile(truststorePath), password)
                    .build();
            CloseableHttpClient closeableHttpClient = HttpClientBuilder
                    .create()
                    .useSystemProperties()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
            return new ApacheHttpClient(closeableHttpClient);
        } else {
            return new ApacheHttpClient();
        }
    }

    @Bean
    ServiceInstanceListSupplier serviceStatusInstanceListSupplier() {
        return new ServiceServerListInstanceSupplier(SERVICE_NAME, infoServiceLoadBalancerConfiguration);
    }

    @Bean
    Retryer retryer() {
        return new Retryer.Default(
                infoServiceLoadBalancerConfiguration.getReadTimeout(),
                infoServiceLoadBalancerConfiguration.getReadTimeout(),
                infoServiceLoadBalancerConfiguration.getRetries()
        );
    }

    @Bean
    Request.Options requestOptions() {
        return new Request.Options(
                infoServiceLoadBalancerConfiguration.getConnectionTimeout(),
                TimeUnit.MILLISECONDS,
                infoServiceLoadBalancerConfiguration.getReadTimeout(),
                TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            FeignException exception = FeignException.errorStatus(methodKey, response);
            if (exception.status() == 404 || exception.status() == 500) {
                // Throw RetryableException, then Retryer can retry request N times
                return new RetryableException(
                        response.status(),
                        exception.getMessage(),
                        response.request().httpMethod(),
                        exception,
                        null,
                        response.request()
                );
            } else {
                // Default for the rest
                return exception;
            }
        };
    }
}