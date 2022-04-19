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

public class TruststoreData {
    private final String path;
    private final String alias;
    private final char[] password;

    public TruststoreData(String path, String alias, char[] password) {
        this.path = path;
        this.alias = alias;
        this.password = password;
    }

    public String getPath() {
        return path;
    }

    public String getAlias() {
        return alias;
    }

    public char[] getPassword() {
        return password;
    }
}
