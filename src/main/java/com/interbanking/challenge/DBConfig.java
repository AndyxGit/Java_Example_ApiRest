package com.interbanking.challenge;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DBConfig {
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/db.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUser() {
        return properties.getProperty("db.user");
    }

    public static String getPassword() {
        return properties.getProperty("db.password");
    }
}