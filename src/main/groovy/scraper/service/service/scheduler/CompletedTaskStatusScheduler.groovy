package scraper.service.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.service.amqp.producer.TaskProducer
import scraper.service.constants.PipelineStatuses
import scraper.service.model.Task
import scraper.service.service.TaskService
import scraper.service.service.UserService

@Component
class CompletedTaskStatusScheduler extends AbstractTaskStatusScheduler {

    CompletedTaskStatusScheduler(TaskService taskService) {
        this.taskService = taskService
    }

    @Autowired
    UserService userService

    @Autowired
    TaskProducer taskProducer

    @Override
    Boolean checkTaskStatus(Task task) {
        if (task.status != PipelineStatuses.COMPLETED) {
            return false
        }
        return false
        // todo: в таком случае запустится следующая задача, которая в ожидании, а root задача с зависимостями нет
        if (userService.hasFreeWorker(task.userId)) {
            Task needRunTask = findNeedRunTasks()
            changeStatus(needRunTask.id, PipelineStatuses.QUEUE)
            taskProducer.taskRun(needRunTask.id)
        }
    }

    private List<Task> findNeedRunTasks() {
        return taskService.findNeedRunTasks()
    }
}
