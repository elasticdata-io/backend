package scraper.service.scheduler

abstract class AbstractTaskStatusScheduler implements TaskStatusScheduler {
    protected scraper.service.TaskService taskService

    void changeStatus(String taskId, String newStatus) {
        taskService.updateStatus(taskId, newStatus)
    }
}
