package com.binderqueue.master;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class App
{
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        InputStream input = new FileInputStream("config/master.properties");
        properties.load(input);
        Master client = new Master(properties);
        client.startServer();
    }
}
