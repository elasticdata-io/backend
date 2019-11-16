package scraper.service.service.scheduler

import scraper.service.service.TaskService

abstract class AbstractTaskStatusScheduler implements TaskStatusScheduler {
    protected TaskService taskService

    void changeStatus(String taskId, String newStatus) {
        taskService.updateStatus(taskId, newStatus)
    }
}
