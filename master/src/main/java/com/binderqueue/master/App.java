package com.binderqueue.master;

public class App
{
    public static void main(String[] args) throws Exception {
        String userName = "";
        String password = "";
        String hostName = "localhost";
        int portNumber = 5672;
        Master client = new Master(userName, password, hostName, portNumber);
        client.startServer();
    }
}
