package com.example.feign.client.info;

import com.example.feign.client.common.ServiceConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("service.info")
public class InfoServiceServiceConfiguration implements ServiceConfiguration {

    private final long connectionTimeout;
    private final long readTimeout;
    private final int retries;
    private final String paths;

    @ConstructorBinding
    public InfoServiceServiceConfiguration(long connectionTimeout, long readTimeout, int retries, String paths) {
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.retries = retries;
        this.paths = paths;
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
}