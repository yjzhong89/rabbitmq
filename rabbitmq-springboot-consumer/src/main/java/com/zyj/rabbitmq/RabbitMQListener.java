package com.zyj.rabbitmq;

import com.rabbitmq.client.Channel;
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
    public void listenerQueue(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            //1.接收转换消息
            System.out.println(new String(message.getBody()));

            //2. 处理业务逻辑
            System.out.println("处理业务逻辑...");
            //3. 手动签收
            channel.basicAck(deliveryTag, true);
        } catch (Exception e) {
            //e.printStackTrace();
            //4.拒绝签收
            /*
            第三个参数：requeue：重回队列。如果设置为true，则消息重新回到queue，broker会重新发送该消息给消费端
             */
            channel.basicNack(deliveryTag, true, true);
        }
    }
}
