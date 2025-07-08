package conjunta.notification_dispatcher.config;

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
    
    public static final String ALERT_EXCHANGE = "alert.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String ALL_ALERTS_ROUTING_KEY = "alert.*";
    public static final String INACTIVE_SENSOR_QUEUE = "inactive.sensor.queue";
    public static final String DAILY_REPORT_QUEUE = "daily.report.queue";
    
    @Bean
    public Queue inactiveSensorQueue() {
        return QueueBuilder.durable(INACTIVE_SENSOR_QUEUE).build();
    }
    
    @Bean
    public Queue dailyReportQueue() {
        return QueueBuilder.durable(DAILY_REPORT_QUEUE).build();
    }
    
    @Bean
    public Binding inactiveSensorBinding() {
        return BindingBuilder
                .bind(inactiveSensorQueue())
                .to(new TopicExchange(ALERT_EXCHANGE))
                .with("alert.sensor.inactive");
    }
    
    @Bean
    public Binding dailyReportBinding() {
        return BindingBuilder
                .bind(dailyReportQueue())
                .to(new TopicExchange(ALERT_EXCHANGE))
                .with("report.daily.generated");
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(new TopicExchange(ALERT_EXCHANGE))
                .with(ALL_ALERTS_ROUTING_KEY);
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