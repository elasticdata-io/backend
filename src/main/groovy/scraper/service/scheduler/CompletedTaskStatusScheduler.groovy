package scraper.service.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.amqp.producer.TaskProducer
import scraper.constants.PipelineStatuses
import scraper.model.Task
import scraper.service.TaskService
import scraper.service.UserService

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
    Boolean checkChangeTaskStatus(Task task) {
        if (task.status != PipelineStatuses.COMPLETED) {
            return false
        }
        return false
        // todo: в таком случае запустится следующая задача, которая в ожидании, а root задача с зависимостями нет
//        if (userService.hasFreeWorker(task.userId)) {
//            scraper.model.Task needRunTask = findNeedRunTasks()
//            changeStatus(needRunTask.id, scraper.constants.PipelineStatuses.QUEUE)
//            taskProducer.taskRun(needRunTask.id)
//        }
    }

    private List<Task> findNeedRunTasks() {
        return taskService.findNeedRunTasks()
    }
}
