/*
 * This code is unpublished proprietary trade secret of
 * Visiona Sp. z o.o., ul. Życzkowskiego 14, 31-864 Kraków, Poland.
 *
 * This code is protected under Act on Copyright and Related Rights
 * and may be used only under the terms of license granted by
 * Visiona Sp. z o.o., ul. Życzkowskiego 14, 31-864 Kraków, Poland.
 *
 * Above notice must be preserved in all copies of this code.
 */

package com.example.feign.server.info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${server.ssl.enabled}")
    private boolean isSecured;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (isSecured) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure())
                    .authorizeRequests(authorize -> authorize.anyRequest().permitAll());
        }
        return http.build();
    }

}