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

    @Autowired
    TaskDependenciesScheduler taskDependenciesScheduler

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
            case PipelineStatuses.WAIT_OTHER_PIPELINE:
                // handleWaitOtherPipelineTask(task)
                break
            default:
                return
        }
    }

    void handleWaitOtherPipelineTask(Task task) {
        def depsFinished = task.taskDependencies.every {dep ->
            def status = dep.dependencyTaskStatus
            return status == PipelineStatuses.COMPLETED || status == PipelineStatuses.ERROR  || status == PipelineStatuses.STOPPED
        }
        if (depsFinished) {
            task.status = PipelineStatuses.QUEUE
            taskService.update(task)
        }
    }

    private void handleFinishedTask(Task task) {
        runWaitingTask(task.userId)
        Task parentTask = taskDependenciesScheduler.updateParentTask(task)
        if (parentTask) {
            taskService.silentUpdate(parentTask)
        }
    }

    /**
     * push to rabbitmq task need stop
     * @param task
     */
    private void handleStoppingTask(Task task) {
        taskProducer.taskStop(task.id)
    }

    private void handlePendingTask(Task task) {
        if (!hasFreeWorker(task.userId)) {
            return
        }
        checkDependencies(task)
    }

    /**
     * push task to rabbitmq queue
     * @param task
     */
    private void handleQueueTask(Task task) {
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
        checkDependencies(waitingTask)
    }

    private void checkDependencies(Task waitingTask) {
        List<Task> taskDependencies = taskDependenciesScheduler.createTaskDependencies(waitingTask)
        waitingTask.status = taskDependencies.isEmpty()
                ? PipelineStatuses.QUEUE
                : PipelineStatuses.WAIT_OTHER_PIPELINE
        taskService.update(waitingTask)
        taskService.update(taskDependencies)
    }
}
