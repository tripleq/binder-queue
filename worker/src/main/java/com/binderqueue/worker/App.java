package com.binderqueue.worker;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class App {
    public static void main(String[] args) throws TimeoutException, IOException {
        String userName = "";
        String password = "";
        String hostName = "localhost";
        int portNumber = 5672;
        Worker worker = new Worker(userName, password, hostName, portNumber);
        worker.listen();
    }
}
