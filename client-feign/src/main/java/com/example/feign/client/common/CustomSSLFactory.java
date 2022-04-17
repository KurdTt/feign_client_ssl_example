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

package com.example.feign.client.common;

import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.util.ResourceUtils;

public class CustomSSLFactory {

    public static SSLSocketFactory create(String path, char[] password) throws NoSuchAlgorithmException {
        try {
            return SSLContextBuilder
                    .create()
                    //.loadKeyMaterial(ResourceUtils.getFile(truststorePath), allPassword, allPassword)
                    .loadTrustMaterial(ResourceUtils.getFile(path), password)
                    .build()
                    .getSocketFactory();
        } catch (Exception e) {
            return SSLContext.getDefault().getSocketFactory();
        }
    }

}
