package com.zyj.rabbitmq.routing;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author yjzhong
 * @date 2020/9/22 9:34
 */
public class Consumer_Routing1 {
    public static void main(String[] args) throws IOException, TimeoutException {
        //1.创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //2. 设置参数
        factory.setHost("192.168.17.144");//ip  默认值 localhost
        factory.setPort(5672); //端口  默认值 5672
        factory.setVirtualHost("/test");//虚拟机 默认值/
        factory.setUsername("test");//用户名 默认 guest
        factory.setPassword("test");//密码 默认值 guest
        //3. 创建连接 Connection
        Connection connection = factory.newConnection();
        //4. 创建Channel
        Channel channel = connection.createChannel();

        String queue1Name = "test_direct_queue1";

        // 接收消息
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println("body：" + new String(body));
                System.out.println("将日志信息打印到控制台.....");
            }
        };
        channel.basicConsume(queue1Name, true, consumer);
        //关闭资源？不要
    }
}
