package com.zyj.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author yjzhong
 * @date 2020/9/22 15:57
 */
@Component
public class RabbitMQListener {
    @RabbitListener(queues = "boot_topic_queue")
    public void listenerQueue(Message message) {
        System.out.println(message);
    }
}
