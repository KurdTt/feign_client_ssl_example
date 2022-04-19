package com.example.feign.client.info;

import com.example.feign.client.common.ServiceConfiguration;
import com.example.feign.client.common.TruststoreData;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("service.info")
public class InfoServiceServiceConfiguration implements ServiceConfiguration {

    private final TruststoreData truststore;
    private final long connectionTimeout;
    private final long readTimeout;
    private final int retries;
    private final String paths;
    private final boolean secured;

    @ConstructorBinding
    public InfoServiceServiceConfiguration(TruststoreData truststore,
                                           long connectionTimeout,
                                           long readTimeout,
                                           int retries,
                                           String paths,
                                           boolean secured) {
        this.truststore = truststore;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.retries = retries;
        this.paths = paths;
        this.secured = secured;
    }

    @Override
    public TruststoreData getTruststore() {
        return truststore;
    }

    @Override
    public String getPaths() {
        return paths;
    }

    @Override
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public long getReadTimeout() {
        return readTimeout;
    }

    @Override
    public int getRetries() {
        return retries;
    }

    @Override
    public boolean isSecured() {
        return secured;
    }
}