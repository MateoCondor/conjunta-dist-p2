package conjunta.sensor_data_collector.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String SENSOR_READING_EXCHANGE = "sensor.reading.exchange";
    public static final String SENSOR_READING_QUEUE = "sensor.reading.queue";
    public static final String SENSOR_READING_ROUTING_KEY = "sensor.reading.new";
    
    @Bean
    public TopicExchange sensorReadingExchange() {
        return new TopicExchange(SENSOR_READING_EXCHANGE);
    }
    
    @Bean
    public Queue sensorReadingQueue() {
        return QueueBuilder.durable(SENSOR_READING_QUEUE).build();
    }
    
    @Bean
    public Binding sensorReadingBinding() {
        return BindingBuilder
                .bind(sensorReadingQueue())
                .to(sensorReadingExchange())
                .with(SENSOR_READING_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}