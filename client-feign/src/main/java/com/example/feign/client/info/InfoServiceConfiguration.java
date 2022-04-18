package com.example.feign.client.info;

import com.example.feign.client.common.CustomSSLFactory;
import com.example.feign.client.common.ServiceServerListInstanceSupplier;
import feign.Client;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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
    Client sslClient(@Qualifier(SERVICE_NAME) SSLSocketFactory sslSocketFactory) {
        return new Client.Default(sslSocketFactory, new NoopHostnameVerifier());
    }


    @Bean
    @Qualifier(SERVICE_NAME)
    SSLSocketFactory sslSocketFactory() throws Exception {
        char[] password = infoServiceLoadBalancerConfiguration.getTruststore().getPassword();
        String truststorePath = infoServiceLoadBalancerConfiguration.getTruststore().getPath();
        return CustomSSLFactory.create(truststorePath, password);
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

    /**
     * Funkcja przesłaniająca nam zachowanie w przypadku konkretnych statusów HTTP.
     *
     * @return Rzucany wyjątek. <code>RetryableException</code> powoduje, że łapiemy się w reguły
     * Retryera, jeżeli to jest jakiś inny wyjątek to po prostu wywołanie jest jednokrotne.
     */
    @Bean
    ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            FeignException exception = FeignException.errorStatus(methodKey, response);
            if (exception.status() == 404 || exception.status() == 500) {
                // Gdy jest 404 rzucamy RetryableException, który pozwala
                // wznowić request tyle razy, ile wynosi getRetries
                return new RetryableException(
                        response.status(),
                        exception.getMessage(),
                        response.request().httpMethod(),
                        exception,
                        null,
                        response.request()
                );
            } else {
                // Pozostałe kody procesujemy domyślnie
                return exception;
            }
        };
    }
}