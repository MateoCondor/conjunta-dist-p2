package conjunta.environmental_analyzer.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // Consumir eventos de sensor readings
    public static final String SENSOR_READING_EXCHANGE = "sensor.reading.exchange";
    public static final String ENVIRONMENTAL_QUEUE = "environmental.analysis.queue";
    public static final String SENSOR_READING_ROUTING_KEY = "sensor.reading.new";
    
    // Publicar alertas
    public static final String ALERT_EXCHANGE = "alert.exchange";
    public static final String HIGH_TEMP_ROUTING_KEY = "alert.temperature.high";
    public static final String SENSOR_INACTIVE_ROUTING_KEY = "alert.sensor.inactive";
    
    @Bean
    public TopicExchange alertExchange() {
        return new TopicExchange(ALERT_EXCHANGE);
    }
    
    @Bean
    public Queue environmentalQueue() {
        return QueueBuilder.durable(ENVIRONMENTAL_QUEUE).build();
    }
    
    @Bean
    public Binding environmentalBinding() {
        return BindingBuilder
                .bind(environmentalQueue())
                .to(new TopicExchange(SENSOR_READING_EXCHANGE))
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
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}