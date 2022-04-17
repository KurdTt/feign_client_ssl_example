package com.example.feign.client.info;

import com.example.feign.client.common.ServiceServerListInstanceSupplier;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ResourceUtils;

public class InfoServiceConfiguration {

    public static final String SERVICE_NAME = "server-info";

    private final InfoServiceServiceConfiguration infoServiceLoadBalancerConfiguration;

    public InfoServiceConfiguration(InfoServiceServiceConfiguration infoServiceLoadBalancerConfiguration) {
        this.infoServiceLoadBalancerConfiguration = infoServiceLoadBalancerConfiguration;
    }

    @Bean
    SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException {
        char[] allPassword = "changeit".toCharArray();
        String truststorePath = "./client-feign/src/main/resources/certs.p12";
        try {
            return SSLContextBuilder
                    .create()
                    .loadKeyMaterial(ResourceUtils.getFile(truststorePath), allPassword, allPassword)
                    .build()
                    .getSocketFactory();
        } catch (Exception e) {
            return SSLContext.getDefault().getSocketFactory();
        }
    }

    @Bean
    ServiceInstanceListSupplier serviceStatusInstanceListSupplier() {
        return new ServiceServerListInstanceSupplier(SERVICE_NAME, infoServiceLoadBalancerConfiguration);
    }

    @Bean
    Retryer retryer() {
        return new Retryer.Default(
                1000L,
                1000L,
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
            if (exception.status() == 404) {
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