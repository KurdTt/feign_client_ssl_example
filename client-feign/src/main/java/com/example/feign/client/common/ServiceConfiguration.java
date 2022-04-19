package com.example.feign.client.common;

public interface ServiceConfiguration {
    TruststoreData getTruststore();

    String getPaths();

    long getConnectionTimeout();

    long getReadTimeout();

    int getRetries();

    boolean isSecured();
}