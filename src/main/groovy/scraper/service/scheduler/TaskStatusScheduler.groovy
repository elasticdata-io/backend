package scraper.service.scheduler

interface TaskStatusScheduler {
    Boolean checkChangeTaskStatus(scraper.model.Task task)
}