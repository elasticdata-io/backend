package scraper.service.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import scraper.service.amqp.producer.TaskProducer
import scraper.service.constants.TaskQueueStatuses
import scraper.service.dto.model.task.PendingTaskDto
import scraper.service.model.TaskQueue
import scraper.service.repository.TaskQueueRepository

@Service
class TaskQueueService {

    @Autowired
    UserService userService

    @Autowired
    TaskProducer taskProducer

    @Autowired
    TaskQueueRepository taskQueueRepository

    /**
     * @param taskId
     * @return
     */
    private triggerRunAfterTask(String taskId) {
        def finishedTaskQueue = taskQueueRepository.findOneByTaskId(taskId)
        if (!finishedTaskQueue) {
            return
        }
        List<TaskQueue> tasks = findWaitingTasksByUser(finishedTaskQueue.userId)
        if (tasks.isEmpty()) {
            return
        }
        def taskQueue = tasks.first()
        taskQueue.status = TaskQueueStatuses.RUNNING
        update(taskQueue)
        taskProducer.taskRun(taskQueue.taskId)
    }

    /**
     * @param taskId
     * @return
     */
    private triggerRun(TaskQueue taskQueue) {
        List<TaskQueue> runningTasks = findRunnningTasksByUser(taskQueue.userId)
        if (runningTasks.size() >= userService.maxAvailableWorkers) {
            return
        }
        taskQueue.status = TaskQueueStatuses.RUNNING
        update(taskQueue)
        taskProducer.taskRun(taskQueue.taskId)
    }

    void toRunTaskQueue(PendingTaskDto pendingTaskDto) {
        TaskQueue taskQueue = new TaskQueue(
                createdOnUtc: new Date(),
                pipelineId: pendingTaskDto.pipelineId,
                taskId: pendingTaskDto.id,
                userId: pendingTaskDto.userId,
                status: TaskQueueStatuses.ADDED
        )
        update(taskQueue)
        triggerRun(taskQueue)
    }

    void toStopTaskQueue(String taskId) {
        remove(taskId)
        taskProducer.taskStop(taskId)
    }

    void toFinishedTaskQueue(String taskId) {
        remove(taskId)
        triggerRunAfterTask(taskId)
    }

    private void remove(String taskId) {
        TaskQueue taskQueue = taskQueueRepository.findOneByTaskId(taskId)
        if (!taskQueue) {
            return
        }
        taskQueue.status = TaskQueueStatuses.FINISHED
        taskQueueRepository.save(taskQueue)
    }

    private TaskQueue update(TaskQueue taskQueue) {
        return taskQueueRepository.save(taskQueue)
    }

    private List<TaskQueue> findWaitingTasksByUser(String userId) {
        return taskQueueRepository.findByStatusAndUserIdOrderByCreatedOnUtc(TaskQueueStatuses.ADDED, userId)
    }

    private List<TaskQueue> findRunnningTasksByUser(String userId) {
        return taskQueueRepository.findByStatusAndUserIdOrderByCreatedOnUtc(TaskQueueStatuses.RUNNING, userId)
    }
}
