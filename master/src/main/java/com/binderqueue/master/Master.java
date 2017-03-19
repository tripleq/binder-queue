package com.binderqueue.master;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Master {
    String userName;
    String password;
    String hostName;
    int portNumber;

    public Master(String userName, String password, String hostName, int portNumber) {
        this.userName = userName;
        this.password = password;
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    public void startServer() throws Exception {
        Server server = new Server(8080);
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("");
        Connection connection = createMQConnection();
        createQueue(connection);
        handler.addServlet(new ServletHolder(new ServletContainer(resourceConfig(connection))), "/*");
        server.setHandler(handler);
        try {
            server.start();
            server.join();
        } finally {
            server.destroy();
        }
    }

    private void createQueue(Connection connection) throws IOException, TimeoutException {
        String QUEUE_NAME = "tasks";
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.close();
    }

    private Connection createMQConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        //factory.setUsername(userName);
        //factory.setPassword(password);
        factory.setHost(hostName);
        //factory.setPort(portNumber);
        return factory.newConnection();

    }

    private ResourceConfig resourceConfig(Connection connection) {
        return new ResourceConfig().register(new EntryPoint(connection));
    }
}
