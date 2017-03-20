package com.binderqueue.master;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

@Path("/")
public class EntryPoint {
    private Connection connection;
    final Logger logger = LoggerFactory.getLogger(EntryPoint.class);

    public EntryPoint(Connection connection) {
        this.connection = connection;
    }

    @POST
    @Path("run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response run(TaskDTO request) {
        final String corrId = UUID.randomUUID().toString();
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
        String requestQueue = "rpc_queue";
        Channel channel = null;
        ResultDTO result = null;
        logger.info("Got request: {}", request);
        try {
            channel = connection.createChannel();
            mapper.writeValue(out, request);
            String replyQueue = channel.queueDeclare().getQueue();

            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueue)
                    .build();

            channel.basicPublish("", requestQueue, props, out.toByteArray());
            logger.info("Sent task with corrID {} to {}. Response queue: {}", corrId, requestQueue, replyQueue);

            channel.basicConsume(replyQueue, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        response.offer(new String(body, "UTF-8"));
                    }
                }
            });

            result = mapper.readValue(response.take(), ResultDTO.class);
            logger.info("Received results for corrID {}, {}", corrId, result);
        } catch (IOException e) {
            logger.warn("{}", e.getStackTrace());
        } catch (InterruptedException e) {
            logger.warn("{}", e.getStackTrace());
        } finally {
            try {
                if(channel != null && channel.isOpen())
                    channel.close();
            } catch (IOException e) {
                logger.warn("{}", e.getStackTrace());
            } catch (TimeoutException e) {
                logger.warn("{}", e.getStackTrace());
            }
        }
        return Response.status(200).entity(result).build();
    }
}