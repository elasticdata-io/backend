package scraper.service.config


import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.backoff.FixedBackOffPolicy
import scraper.service.amqp.QueueConstants

@Configuration
class RabbitConfiguration {

    final int CONCURRENT_CONSUMERS = 3

    final int MAX_CONCURRENT_CONSUMERS = 5

    @Value('${spring.rabbitmq.host}')
    String host

    @Value('${spring.rabbitmq.username}')
    String user

    @Value('${spring.rabbitmq.password}')
    String password

    @Value('${spring.rabbitmq.port}')
    String port

    @Value('${spring.rabbitmq.topicExchangeName}')
    String topicExchangeName

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host)
        connectionFactory.setUsername(user)
        connectionFactory.setPassword(password)
        connectionFactory.setPort(Integer.parseInt(port))
        return connectionFactory
    }

    @Bean(name = "defaultConnectionFactory")
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setConcurrentConsumers(CONCURRENT_CONSUMERS)
        factory.setMaxConcurrentConsumers(MAX_CONCURRENT_CONSUMERS)
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy()
        backOffPolicy.setBackOffPeriod(500)
        factory
            .setAdviceChain(RetryInterceptorBuilder.stateless()
            .maxAttempts(1)
            .backOffPolicy(backOffPolicy)
            .build())
        return factory
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName, false, true)
    }

    @Bean
    Queue pipelineRunQueue() {
        return new Queue(QueueConstants.PIPELINE_RUN)
    }

    @Bean
    Binding bindPipelineRun(final Queue pipelineRunQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineRunQueue)
                .to(exchange)
                .with(QueueConstants.PIPELINE_RUN)
    }

    @Bean
    Queue pipelineStopQueue() {
        return new Queue(QueueConstants.PIPELINE_STOP)
    }

    @Bean
    Binding bindPipelineStop(final Queue pipelineStopQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineStopQueue)
                .to(exchange)
                .with(QueueConstants.PIPELINE_STOP)
    }

    @Bean
    Queue pipelineTaskFinishQueue() {
        return new Queue(QueueConstants.PIPELINE_TASK_FINISH)
    }

    @Bean
    Binding bindPipelineTaskFinish(final Queue pipelineTaskFinishQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineTaskFinishQueue)
                .to(exchange)
                .with(QueueConstants.PIPELINE_TASK_FINISH)
    }

    @Bean
    Queue pipelineRunHierarchyQueue() {
        return new Queue(QueueConstants.PIPELINE_RUN_HIERARCHY)
    }

    @Bean
    Binding bindPipelineRunHierarchy(final Queue pipelineRunHierarchyQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineRunHierarchyQueue)
                .to(exchange)
                .with(QueueConstants.PIPELINE_RUN_HIERARCHY)
    }

}