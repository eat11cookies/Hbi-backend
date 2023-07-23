package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class BiinitMain {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("140.143.139.251");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = BiConstant.BI_EXCHANGE_NAME;
            //声明交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
//            Map<String, Object> arg= new HashMap<String, Object>();
//            arg.put("x-message-ttl", 100000);
            String queueName = BiConstant.BI_QUEUE_NAME;
            //创建队列
            channel.basicQos(2);
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, BiConstant.BI_ROUTING_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
