package scraper.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.backoff.FixedBackOffPolicy
import scraper.amqp.RoutingConstants

@Configuration
class RabbitConfiguration {

    final int CONCURRENT_CONSUMERS = 1

    final int MAX_CONCURRENT_CONSUMERS = 2

    @Value('${spring.rabbitmq.host}')
    String host

    @Value('${spring.rabbitmq.username}')
    String user

    @Value('${spring.rabbitmq.password}')
    String password

    @Value('${spring.rabbitmq.port}')
    String port

    @Value('${spring.rabbitmq.exchange.runTask}')
    String runTaskExchangeName

    @Value('${spring.rabbitmq.exchange.inboxFanout}')
    String inboxFanoutExchangeName

    @Autowired
    scraper.amqp.QueueConstants queueConstants

    @Autowired
    RoutingConstants routingConstants

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
        factory.setPrefetchCount(1)
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy()
        backOffPolicy.setBackOffPeriod(500)
        factory
            .setAdviceChain(RetryInterceptorBuilder.stateless()
            .maxAttempts(2)
            .backOffPolicy(backOffPolicy)
            .build())
        return factory
    }

    @Bean
    FanoutExchange inboxFanoutExchange() {
        return new FanoutExchange(inboxFanoutExchangeName, true, false)
    }

    @Bean
    TopicExchange runTaskExchange() {
        return new TopicExchange(runTaskExchangeName, true, false)
    }

    @Bean
    Queue runHookQueue() {
        return new Queue(queueConstants.RUN_HOOK)
    }

    @Bean
    Binding bindRunHook(final Queue runHookQueue, final TopicExchange runTaskExchange) {
        return BindingBuilder
                .bind(runHookQueue)
                .to(runTaskExchange)
                .with(routingConstants.RUN_HOOK_ROUTING_KEY)
    }
}