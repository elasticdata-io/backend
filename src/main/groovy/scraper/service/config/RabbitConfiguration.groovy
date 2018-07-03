package scraper.service.config

import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfiguration {

    Number CONCURRENT_CONSUMERS = 5

    Number MAX_CONCURRENT_CONSUMERS = 10

    @Value('${spring.rabbitmq.host}')
    String host

    @Value('${spring.rabbitmq.username}')
    String user

    @Value('${spring.rabbitmq.password}')
    String password

    @Value('${spring.rabbitmq.port}')
    String port

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host)
        connectionFactory.setUsername(user)
        connectionFactory.setPassword(password)
        connectionFactory.setPort(port.toInteger())
        println host
        println user
        println password
        println port
        return connectionFactory
    }

    @Bean
    SimpleRabbitListenerContainerFactory multipleListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory())
        factory.setConcurrentConsumers(CONCURRENT_CONSUMERS)
        factory.setMaxConcurrentConsumers(MAX_CONCURRENT_CONSUMERS)
        return factory
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

    @Bean
    Queue pipelineRunHierarchyQueue() {
        return new Queue("pipeline-run-hierarchy")
    }
}