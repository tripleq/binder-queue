package com.binderqueue.worker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class App {
    public static void main(String[] args) throws TimeoutException, IOException {
        Properties properties = new Properties();
        InputStream input = new FileInputStream("config/worker.properties");
        properties.load(input);
        Worker worker = new Worker(properties);
        worker.listen();
    }
}
