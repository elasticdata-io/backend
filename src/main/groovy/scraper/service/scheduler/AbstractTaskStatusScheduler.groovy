package scraper.service.scheduler

import scraper.service.TaskService

abstract class AbstractTaskStatusScheduler implements TaskStatusScheduler {
    protected TaskService taskService

    void changeStatus(String taskId, String newStatus) {
        taskService.updateStatus(taskId, newStatus)
    }
}
