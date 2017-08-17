package scraper.service.config

import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfiguration {

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost")
        return connectionFactory
    }

    @Bean
    AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory())
    }

    @Bean
    RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory())
    }

    @Bean
    Queue pipelineRunQueue() {
        return new Queue("pipeline-run-queue")
    }
}