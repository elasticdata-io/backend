package scraper.service.workermanager

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scraper.constants.PipelineStatuses
import scraper.service.TariffPlanService
import scraper.service.TaskService
import scraper.service.UserService

@Component
class WorkersScaler {
    @Autowired
    WorkerManagerClient workerManagerClient

    @Autowired
    UserService userService

    @Autowired
    TaskService taskService

    @Autowired
    TariffPlanService tariffPlanService

    void checkReplicasDown(String userId) {
        def notFreeSubscription = tariffPlanService.notFreeSubscription(userId)
        if(!notFreeSubscription) {
            return
        }
        List<String> inProcessingStatuses = PipelineStatuses.getInProcessing()
        def tasks = taskService.findByStatusInAndUserId(inProcessingStatuses, userId)
        if (tasks.isEmpty()) {
            workerManagerClient.scale(userId, 0)
        }
    }


    void checkReplicasUp(String userId) {
        def notFreeSubscription = tariffPlanService.notFreeSubscription(userId)
        if(!notFreeSubscription) {
            return
        }
        def statuses = [PipelineStatuses.QUEUE, PipelineStatuses.PENDING]
        def pendingTasks = taskService.findByStatusInAndUserId(statuses, userId)
        int desiredWorkers = pendingTasks.size() as int
        def actualWorkers = workerManagerClient.fetchReplicas(userId)
        if (desiredWorkers > actualWorkers) {
            int maxAvailableWorkers = userService.getMaxAvailableWorkers(userId) as int
            def scaleTo = Math.min(maxAvailableWorkers, desiredWorkers)
            workerManagerClient.scale(userId, scaleTo)
        }
    }
}
