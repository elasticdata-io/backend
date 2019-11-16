package scraper.service.service.scheduler

import scraper.service.model.Task

interface TaskStatusScheduler {
    Boolean checkTaskStatus(Task task)
}