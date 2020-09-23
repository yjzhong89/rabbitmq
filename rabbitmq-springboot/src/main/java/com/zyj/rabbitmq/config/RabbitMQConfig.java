package com.zyj.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yjzhong
 * @date 2020/9/22 15:10
 */
@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "boot_topic_exchange";
    public static final String QUEUE_NAME = "boot_topic_queue";

    @Bean("bootExchange")
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    @Bean("bootQueue")
    public Queue queue() {
        return QueueBuilder.durable(QUEUE_NAME).withArgument("x-message-ttl", 100000).build();
    }

    @Bean
    public Binding bindQueueExchange(@Qualifier("bootQueue") Queue queue, @Qualifier("bootExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("code.#").noargs();
    }

    // 死信队列
    public static final String TEST_DLX_EXCHANGE_NAME = "test_dlx_exchange";
    public static final String TEST_DLX_QUEUE_NAME = "test_dlx_queue";

    public static final String DLX_EXCHANGE_NAME = "dlx_exchange";
    public static final String DLX_QUEUE_NAME = "dlx_queue";

    @Bean("testDLXExchange")
    public Exchange testDLXExchange() {
        return ExchangeBuilder.topicExchange(TEST_DLX_EXCHANGE_NAME).durable(true).build();
    }

    @Bean("testDLXQueue")
    public Queue testDLXQueue() {
        return QueueBuilder.durable(TEST_DLX_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", "dlx.test")
                .withArgument("x-message-ttl", 10000)
                .withArgument("x-max-length", 10)
                .build();
    }

    @Bean
    public Binding testDLXBindQueueExchange(@Qualifier("testDLXQueue") Queue queue, @Qualifier("testDLXExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("test.dlx.#").noargs();
    }

    @Bean("dLXExchange")
    public Exchange dLXExchange() {
        return ExchangeBuilder.topicExchange(DLX_EXCHANGE_NAME).durable(true).build();
    }

    @Bean("dLXQueue")
    public Queue dLXqueue() {
        return QueueBuilder.durable(DLX_QUEUE_NAME).build();
    }

    @Bean
    public Binding dLXBindQueueExchange(@Qualifier("dLXQueue") Queue queue, @Qualifier("dLXExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("dlx.#").noargs();
    }
}
