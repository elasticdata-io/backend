package scraper.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CompletedTaskStatusScheduler extends AbstractTaskStatusScheduler {

    CompletedTaskStatusScheduler(scraper.service.TaskService taskService) {
        this.taskService = taskService
    }

    @Autowired
    scraper.service.UserService userService

    @Autowired
    scraper.amqp.producer.TaskProducer taskProducer

    @Override
    Boolean checkChangeTaskStatus(scraper.model.Task task) {
        if (task.status != scraper.constants.PipelineStatuses.COMPLETED) {
            return false
        }
        return false
        // todo: в таком случае запустится следующая задача, которая в ожидании, а root задача с зависимостями нет
        if (userService.hasFreeWorker(task.userId)) {
            scraper.model.Task needRunTask = findNeedRunTasks()
            changeStatus(needRunTask.id, scraper.constants.PipelineStatuses.QUEUE)
            taskProducer.taskRun(needRunTask.id)
        }
    }

    private List<scraper.model.Task> findNeedRunTasks() {
        return taskService.findNeedRunTasks()
    }
}
