package com.samap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for asynchronous message processing
 */
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class MessageQueueConfig {

    // Queue names
    public static final String SECURITY_ALERTS_QUEUE = "samap.security.alerts";
    public static final String AUDIT_EVENTS_QUEUE = "samap.audit.events";
    public static final String NOTIFICATION_QUEUE = "samap.notifications";
    public static final String RISK_ASSESSMENT_QUEUE = "samap.risk.assessment";

    // Exchange names
    public static final String SECURITY_EXCHANGE = "samap.security.exchange";
    public static final String AUDIT_EXCHANGE = "samap.audit.exchange";
    public static final String NOTIFICATION_EXCHANGE = "samap.notification.exchange";

    // Routing keys
    public static final String SECURITY_ALERT_ROUTING_KEY = "security.alert";
    public static final String AUDIT_EVENT_ROUTING_KEY = "audit.event";
    public static final String NOTIFICATION_ROUTING_KEY = "notification";
    public static final String RISK_ASSESSMENT_ROUTING_KEY = "risk.assessment";

    /**
     * Message converter for JSON serialization
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitMQ template configuration
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Rabbit listener container factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    // Security Exchange and Queue
    @Bean
    public TopicExchange securityExchange() {
        return new TopicExchange(SECURITY_EXCHANGE);
    }

    @Bean
    public Queue securityAlertsQueue() {
        return QueueBuilder.durable(SECURITY_ALERTS_QUEUE)
                .withArgument("x-message-ttl", 3600000) // 1 hour TTL
                .build();
    }

    @Bean
    public Binding securityAlertsBinding() {
        return BindingBuilder
                .bind(securityAlertsQueue())
                .to(securityExchange())
                .with(SECURITY_ALERT_ROUTING_KEY);
    }

    // Audit Exchange and Queue
    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE);
    }

    @Bean
    public Queue auditEventsQueue() {
        return QueueBuilder.durable(AUDIT_EVENTS_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 hours TTL
                .build();
    }

    @Bean
    public Binding auditEventsBinding() {
        return BindingBuilder
                .bind(auditEventsQueue())
                .to(auditExchange())
                .with(AUDIT_EVENT_ROUTING_KEY);
    }

    // Notification Exchange and Queue
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 1800000) // 30 minutes TTL
                .build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    // Risk Assessment Queue
    @Bean
    public Queue riskAssessmentQueue() {
        return QueueBuilder.durable(RISK_ASSESSMENT_QUEUE)
                .withArgument("x-message-ttl", 600000) // 10 minutes TTL
                .build();
    }

    @Bean
    public Binding riskAssessmentBinding() {
        return BindingBuilder
                .bind(riskAssessmentQueue())
                .to(securityExchange())
                .with(RISK_ASSESSMENT_ROUTING_KEY);
    }

    // Dead Letter Queue for failed messages
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("samap.dead.letter.queue").build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange("samap.dead.letter.exchange");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dead.letter");
    }

    /**
     * Message publisher service
     */
    @Bean
    public MessagePublisher messagePublisher(RabbitTemplate rabbitTemplate) {
        return new MessagePublisher(rabbitTemplate);
    }

    /**
     * Message publisher utility class
     */
    public static class MessagePublisher {
        private final RabbitTemplate rabbitTemplate;

        public MessagePublisher(RabbitTemplate rabbitTemplate) {
            this.rabbitTemplate = rabbitTemplate;
        }

        public void publishSecurityAlert(Object message) {
            rabbitTemplate.convertAndSend(SECURITY_EXCHANGE, SECURITY_ALERT_ROUTING_KEY, message);
            log.info("Security alert published: {}", message);
        }

        public void publishAuditEvent(Object message) {
            rabbitTemplate.convertAndSend(AUDIT_EXCHANGE, AUDIT_EVENT_ROUTING_KEY, message);
            log.debug("Audit event published: {}", message);
        }

        public void publishNotification(Object message) {
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, NOTIFICATION_ROUTING_KEY, message);
            log.info("Notification published: {}", message);
        }

        public void publishRiskAssessment(Object message) {
            rabbitTemplate.convertAndSend(SECURITY_EXCHANGE, RISK_ASSESSMENT_ROUTING_KEY, message);
            log.debug("Risk assessment published: {}", message);
        }
    }
}
