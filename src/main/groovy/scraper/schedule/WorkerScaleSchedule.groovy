package scraper.schedule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.constants.PipelineStatuses
import scraper.service.TariffPlanService
import scraper.service.TaskService
import scraper.service.UserService
import scraper.service.workermanager.WorkerManagerClient

@Component
class WorkerScaleSchedule {

    @Autowired
    WorkerManagerClient workerManagerClient

    @Autowired
    UserService userService

    @Autowired
    TaskService taskService

    @Autowired
    TariffPlanService tariffPlanService

    @Scheduled(cron='*/120 * * * * * ')
    void checkScaleDownDeployment() {
        def users = userService.findAll()
        users.forEach( {
            def notFreeSubscription = tariffPlanService.notFreeSubscription(it.id)
            if(!notFreeSubscription) {
                return
            }
            List<String> inProcessingStatuses = PipelineStatuses.getInProcessing()
            def tasks = taskService.findByStatusInAndUserId(inProcessingStatuses, it.id)
            if (tasks.isEmpty()) {
                workerManagerClient.scale(it.id, 0)
            }
        })
    }

    @Scheduled(cron='*/30 * * * * * ')
    void checkScaleUpDeployment() {
        def users = userService.findAll()
        users.forEach( {
            def notFreeSubscription = tariffPlanService.notFreeSubscription(it.id)
            if(!notFreeSubscription) {
                return
            }
            def statuses = [PipelineStatuses.QUEUE, PipelineStatuses.PENDING]
            def pendingTasks = taskService.findByStatusInAndUserId(statuses, it.id)
            int desiredWorkers = pendingTasks.size() as int
            def actualWorkers = workerManagerClient.fetchReplicas(it.id)
            if (desiredWorkers > actualWorkers) {
                int maxAvailableWorkers = userService.getMaxAvailableWorkers(it.id) as int
                def scaleTo = Math.min(maxAvailableWorkers, desiredWorkers)
                workerManagerClient.scale(it.id, scaleTo)
            }
        })
    }
}
