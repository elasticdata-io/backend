package scraper.service.config


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
import scraper.service.amqp.QueueConstants
import scraper.service.amqp.RoutingConstants

@Configuration
class RabbitConfiguration {

    final int CONCURRENT_CONSUMERS = 4

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

    @Value('${spring.rabbitmq.exchange.pipeline.stop}')
    String pipelineStopExchangeName

    @Value('${spring.rabbitmq.exchange.executeTaskCommand}')
    String executeTaskCommandExchangeName

    @Autowired
    QueueConstants queueConstants

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
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName, false, true)
    }

    @Bean
    FanoutExchange pipelineStopExchange() {
        return new FanoutExchange(pipelineStopExchangeName, false, true)
    }

    @Bean
    FanoutExchange executeTaskCommandExchange() {
        return new FanoutExchange(executeTaskCommandExchangeName, false, true)
    }

    @Bean
    Queue pipelineTaskStopV1Queue() {
        return new Queue(queueConstants.PIPELINE_TASK_STOP_V1)
    }

    @Bean
    Queue pipelineTaskStopV2Queue() {
        return new Queue(queueConstants.PIPELINE_TASK_STOP_V2)
    }

    @Bean
    Queue executeCommandQueue() {
        return new Queue(queueConstants.EXECUTE_CMD)
    }

    @Bean
    Binding bindPipelineTaskStopV1(final Queue pipelineTaskStopV1Queue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineTaskStopV1Queue)
                .to(exchange)
                .with(routingConstants.PIPELINE_TASK_STOP)
    }

    @Bean
    Binding bindPipelineTaskStopV2(final Queue pipelineTaskStopV2Queue, final FanoutExchange pipelineStopExchange) {
        return BindingBuilder
                .bind(pipelineTaskStopV2Queue)
                .to(pipelineStopExchange)
    }

    @Bean
    Binding bindExecuteCommand(final Queue executeCommandQueue, final FanoutExchange executeTaskCommandExchange) {
        return BindingBuilder
                .bind(executeCommandQueue)
                .to(executeTaskCommandExchange)
    }

    @Bean
    Queue taskChangedQueue() {
        return new Queue(queueConstants.TASK_CHANGED)
    }

    @Bean
    Binding bindTaskChangedRun(final Queue taskChangedQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(taskChangedQueue)
                .to(exchange)
                .with(routingConstants.TASK_CHANGED)
    }

    @Bean
    Queue pipelineTaskRunQueue() {
        return new Queue(queueConstants.PIPELINE_TASK_RUN)
    }

    @Bean
    Binding bindPipelineTaskRun(final Queue pipelineTaskRunQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineTaskRunQueue)
                .to(exchange)
                .with(routingConstants.PIPELINE_TASK_RUN)
    }

    @Bean
    Queue pipelineTaskRunNodeQueue() {
        return new Queue(queueConstants.PIPELINE_TASK_RUN_NODE)
    }

    @Bean
    Binding bindPipelineTaskRunNode(final Queue pipelineTaskRunNodeQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineTaskRunNodeQueue)
                .to(exchange)
                .with(routingConstants.PIPELINE_TASK_RUN_NODE)
    }

    @Bean
    Queue pipelineTaskFinishedQueue() {
        return new Queue(queueConstants.PIPELINE_TASK_FINISHED)
    }

    @Bean
    Binding bindPipelineTaskFinished(final Queue pipelineTaskFinishedQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineTaskFinishedQueue)
                .to(exchange)
                .with(routingConstants.PIPELINE_TASK_FINISH)
    }

    @Bean
    Queue pipelineRunHooksQueue() {
        return new Queue(queueConstants.PIPELINE_RUN_HOOKS)
    }

    @Bean
    Binding bindPipelineRunHooks(final Queue pipelineRunHooksQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineRunHooksQueue)
                .to(exchange)
                .with(routingConstants.PIPELINE_TASK_FINISH)
    }

    @Bean
    Queue pipelineFinishedQueue() {
        return new Queue(queueConstants.PIPELINE_FINISHED)
    }

    @Bean
    Binding bindPipelineFinished(final Queue pipelineFinishedQueue, final TopicExchange exchange) {
        return BindingBuilder
                .bind(pipelineFinishedQueue)
                .to(exchange)
                .with(routingConstants.PIPELINE_FINISH)
    }

}