package com.binderqueue.master;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
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

    public EntryPoint(Connection connection) {
        this.connection = connection;
    }

    @POST
    @Path("run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response run(TaskDTO request) {
        Channel channel;
        ResultDTO result = null;
        System.out.println("Got: " + request);
        try {
            channel = connection.createChannel();
            ObjectMapper mapper = new ObjectMapper();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mapper.writeValue(out, request);
            String replyQueue = channel.queueDeclare().getQueue();
            String requestQueue = "rpc_queue";
            final String corrId = UUID.randomUUID().toString();

            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueue)
                    .build();

            channel.basicPublish("", requestQueue, props, out.toByteArray());
            final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

            channel.basicConsume(replyQueue, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        response.offer(new String(body, "UTF-8"));
                    }
                }
            });
            result = mapper.readValue(response.take(), ResultDTO.class);
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Response.status(200).entity(result).build();
    }
}