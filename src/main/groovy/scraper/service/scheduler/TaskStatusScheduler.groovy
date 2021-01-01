package scraper.service.scheduler

import scraper.model.Task

interface TaskStatusScheduler {
    Boolean checkChangeTaskStatus(Task task)
}