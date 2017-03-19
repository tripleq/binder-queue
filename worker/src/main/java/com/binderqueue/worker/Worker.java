package com.binderqueue.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Worker {
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private final ObjectMapper mapper;

    public Worker(String userName, String password, String hostName, int portNumber) throws IOException, TimeoutException {
        connectionFactory = new ConnectionFactory();
        //connectionFactory.setUsername(userName);
        //connectionFactory.setPassword(password);
        connectionFactory.setHost(hostName);
        //connectionFactory.setPort(portNumber);
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        mapper = new ObjectMapper();
        channel.queueDeclare("rpc_queue", false, false, false, null);
        channel.basicQos(1);
    }

    public void listen() throws IOException {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();

                ResultDTO result = new ResultDTO();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {

                    TaskDTO task = mapper.readValue(body, TaskDTO.class);
                    result.setResult(work(task));
                    mapper.writeValue(outputStream, result);
                }
                catch (RuntimeException e){
                    System.out.println(e.toString());
                }
                finally {
                    channel.basicPublish( "", properties.getReplyTo(), replyProps, outputStream.toByteArray());
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };

        channel.basicConsume("rpc_queue", false, consumer);
    }

    private double work(TaskDTO task) throws IOException {
        //TODO
        Random random = new Random();
        int min = 5000;
        int max = 15000;
        int sleepTime = random.nextInt(max - min + 1) + min;
        System.out.println("Received '" + task + "'");
        System.out.println("Sleeping for " + sleepTime + "ms");

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return sum(task.getParams());
    }

    // temp
    private double sum(List<Double> numbers) {
        double sum = 0.0;
        for (double num : numbers) {
            sum += num;
        }
        return sum;
    }
}
