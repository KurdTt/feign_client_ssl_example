package com.example.feign.client.common;

public interface ServiceConfiguration {
    String getPaths();

    long getConnectionTimeout();

    long getReadTimeout();

    int getRetries();
}