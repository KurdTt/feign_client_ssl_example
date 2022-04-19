package com.example.feign.client.status;

import com.example.feign.client.common.CustomSSLFactory;
import com.example.feign.client.common.ServiceServerListInstanceSupplier;
import feign.Client;
import feign.Request;
import feign.Retryer;
import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.tls.HandshakeCertificates;
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
import org.springframework.util.ResourceUtils;

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
    Client sslClient(@Qualifier(SERVICE_NAME) Optional<SSLSocketFactory> sslSocketFactory,
                     @Qualifier(SERVICE_NAME) Optional<X509TrustManager> x509TrustManager) {
        if (statusServiceLoadBalancerConfiguration.isSecured()
                && x509TrustManager.isPresent()
                && sslSocketFactory.isPresent()) {
            return new feign.okhttp.OkHttpClient(
                    new OkHttpClient.Builder()
                            .sslSocketFactory(sslSocketFactory.get(), x509TrustManager.get())
                            .hostnameVerifier(new NoopHostnameVerifier())
                            .build()
            );
        } else {
            return new feign.okhttp.OkHttpClient();
        }
    }

    @Bean
    @Qualifier(SERVICE_NAME)
    X509TrustManager x509TrustManager() throws Exception {
        if (statusServiceLoadBalancerConfiguration.isSecured()) {
            String truststorePath = statusServiceLoadBalancerConfiguration.getTruststore().getPath();
            char[] password = statusServiceLoadBalancerConfiguration.getTruststore().getPassword();
            File truststore = ResourceUtils.getFile(truststorePath);
            KeyStore keyStore = KeyStore.getInstance(truststore, password);
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate("status");
            return new HandshakeCertificates.Builder()
                    .addTrustedCertificate(certificate)
                    .build()
                    .trustManager();
        } else {
            return null;
        }
    }

    @Bean
    @Qualifier(SERVICE_NAME)
    SSLSocketFactory sslSocketFactory() throws Exception {
        if (statusServiceLoadBalancerConfiguration.isSecured()) {
            char[] password = statusServiceLoadBalancerConfiguration.getTruststore().getPassword();
            String truststorePath = statusServiceLoadBalancerConfiguration.getTruststore().getPath();
            return CustomSSLFactory.create(truststorePath, password);
        } else {
            return null;
        }
    }

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