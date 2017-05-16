package com.binderqueue.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker {
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private final ObjectMapper mapper;
    private final Properties properties;
    final Logger logger = LoggerFactory.getLogger(Worker.class);

    public Worker(Properties properties) throws IOException, TimeoutException {
        this.properties = properties;
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(properties.getProperty("rabbitmq.host"));
        connectionFactory.setPort(Integer.parseInt(properties.getProperty("rabbitmq.port")));
        connectionFactory.setUsername(properties.getProperty("rabbitmq.username"));
        connectionFactory.setPassword(properties.getProperty("rabbitmq.password"));
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
                    try {
                        result.setResult(work(task));
                        result.setStatus("OK");
                    } catch (TaskException e) {
                        result.setStatus("ERROR");
                    } catch (IOException e) {
                        result.setStatus("ERROR");
                    }
                    mapper.writeValue(outputStream, result);
                }
                catch (RuntimeException e){
                    logger.error("Can't read task");
                    result.setStatus("ERROR");
                    mapper.writeValue(outputStream, result);
                }
                finally {
                    channel.basicPublish( "", properties.getReplyTo(), replyProps, outputStream.toByteArray());
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        channel.basicConsume("rpc_queue", false, consumer);
    }

    private List<Double> work(TaskDTO task) throws IOException, TaskException {
        List<Double> params = task.getParams();
        List<Double> results = new ArrayList<Double>();
        String binderDir = properties.getProperty("binderdir");
        String separator = properties.getProperty("separator");
        String executable = properties.getProperty("executable");
        String optimizationDir = properties.getProperty("optimizationdir");
        int optimizationId = task.getOptimizationId();
        UUID uuid = UUID.randomUUID();
        String workingDirLocation = binderDir + separator + uuid.toString();
        File oldExecutable = new File(binderDir + separator + optimizationDir + separator + String.valueOf(optimizationId) + separator + executable);
        File newExecutable = new File(binderDir + separator + uuid.toString() + separator + executable);
        File workingDir = new File(workingDirLocation);
        workingDir.mkdir();
        FileUtils.copyFile(oldExecutable, newExecutable);
        newExecutable.setExecutable(true);

        String commandFullPath = workingDirLocation + separator + properties.getProperty("executable");
        String[] commandArray = {
          commandFullPath, binderDir, optimizationDir, String.valueOf(optimizationId)
        };

        ProcessBuilder pb = new ProcessBuilder(commandArray);

        BufferedWriter input;
        BufferedReader output;
        try {
            final Process pr = pb.start();
            logger.info("Process started");
            input = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));

            input.write(params.size());
            input.newLine();

            for(int i = 0; i < params.size(); i++) {
                input.write(Double.toString(params.get(i)));
                input.newLine();
            }
            input.flush();
            input.close();

            output = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String s = output.readLine();
            logger.info("Process output: {}", s);

            if(s.equalsIgnoreCase("OK")) {
                int len = Integer.parseInt(output.readLine());
                for(int i = 0; i < len; i++) {
                    results.add(Double.parseDouble(output.readLine()));
                }
            } else {
                throw new TaskException("Process did not return OK");
            }
        } catch (TaskException e) {
            logger.error("Can't execute: {}. {}", newExecutable, e.toString());
            throw e;
        } finally {
            if(Boolean.parseBoolean(properties.getProperty("cleanup"))) {
                try {
                    FileUtils.deleteDirectory(workingDir);
                } catch (IOException e) {
                    logger.warn("Can't remove working dir");
                }
            }
        }
        return results;
    }
}
