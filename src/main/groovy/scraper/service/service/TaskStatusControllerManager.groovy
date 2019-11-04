package scraper.service.service

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.amqp.producer.TaskProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Task

@Service
class TaskStatusControllerManager {
    private Logger logger = LogManager.getRootLogger()

    @Autowired
    TaskService taskService

    @Autowired
    TaskProducer taskProducer

    @Autowired
    UserService userService

    void update(Task task) {
        switch(task.status) {
            case PipelineStatuses.PENDING:
                handlePendingTask(task)
                break
            case PipelineStatuses.QUEUE:
                handleQueueTask(task)
                break
            case PipelineStatuses.STOPPING:
                handleStoppingTask(task)
                break
            case PipelineStatuses.STOPPED:
                handleFinishedTask(task)
                break
            case PipelineStatuses.COMPLETED:
                handleFinishedTask(task)
                break
            case PipelineStatuses.ERROR:
                handleFinishedTask(task)
                break
            default:
                return
        }
    }

    void handleFinishedTask(Task task) {
        runWaitingTask(task.userId)
    }

    /**
     * push to rabbitmq task need stop
     * @param task
     */
    void handleStoppingTask(Task task) {
        taskProducer.taskStop(task.id)
    }

    void handlePendingTask(Task task) {
        if (hasFreeWorker(task.userId)) {
            // todo : check deps
            task.status = PipelineStatuses.QUEUE
            taskService.update(task)
        }
    }

    /**
     * push task to rabbitmq queue
     * @param task
     */
    void handleQueueTask(Task task) {
        taskProducer.taskRun(task.id)
    }

    private Boolean hasFreeWorker(String userId) {
        def statuses = [PipelineStatuses.RUNNING, PipelineStatuses.QUEUE]
        List<Task> tasks = taskService.findByStatusInAndUserId(statuses, userId)
        if (tasks.size() >= userService.maxAvailableWorkers) {
            logger.info("not has free worker for user: ${userId}")
            return false
        }
        return true
    }

    private runWaitingTask(String userId) {
        if (!hasFreeWorker(userId)) {
            return
        }
        Task waitingTask = taskService.findFirstWaitingTaskByUserId(userId)
        if (!waitingTask) {
            return
        }
        // todo: check deps
        waitingTask.status = PipelineStatuses.QUEUE
        taskService.update(waitingTask)
    }
}
