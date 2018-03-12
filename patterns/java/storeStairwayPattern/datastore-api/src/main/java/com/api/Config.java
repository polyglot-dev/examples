package com.api;

import com.typesafe.config.ConfigFactory;

public class Config {

    com.typesafe.config.Config config = ConfigFactory.load();

    public String getString(String path) {
        String s = System.getenv(path);
        if (s == null)
            s = config.getString(path);
        return s;
    }

    public int getInt(String path) {
        String s = System.getenv(path);
        return (s == null) ? config.getInt(path) : Integer.parseInt(s);
    }

}
