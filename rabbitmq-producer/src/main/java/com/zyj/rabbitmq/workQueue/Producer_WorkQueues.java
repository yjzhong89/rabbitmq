package com.zyj.rabbitmq.workQueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author yjzhong
 * @date 2020/9/22 9:50
 */
public class Producer_WorkQueues {
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
        //5. 创建队列Queue
        channel.queueDeclare("work_queues", true, false, false, null);
        for (int i = 1; i <= 10; i++) {
            String body = i + "hello rabbitmq~~~";
            //6. 发送消息
            channel.basicPublish("", "work_queues", null, body.getBytes());
        }
        //7.释放资源
        channel.close();
        connection.close();
    }
}
