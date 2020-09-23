package com.zyj.rabbitmq.test;

import com.zyj.rabbitmq.config.RabbitMQConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author yjzhong
 * @date 2020/9/22 15:23
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProducerTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSend() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "code.rabbit", "hello rabbitmq springboot");
    }

    @Test
    public void testTTLSend() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "code.rabbit", "hello rabbitmq springboot", message -> {
            message.getMessageProperties().setExpiration("10000");
            return message;
        });
    }

    @Test
    public void testDLXSend() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.TEST_DLX_EXCHANGE_NAME, "test.dlx.test", "hello rabbitmq springboot");
    }
}
