package scraper.schedule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scraper.service.UserService
import scraper.service.workermanager.WorkersScaler

@Component
class WorkersScaleSchedule {

    @Autowired
    WorkersScaler workersScaler

    @Autowired
    UserService userService

    @Scheduled(cron='*/120 * * * * * ')
    void checkScaleDownDeployment() {
        def users = userService.findAll()
        users.forEach( {
            workersScaler.checkReplicasDown(it.id)
        })
    }

    @Scheduled(cron='*/30 * * * * * ')
    void checkScaleUpDeployment() {
        def users = userService.findAll()
        users.forEach( {
            workersScaler.checkReplicasUp(it.id)
        })
    }
}
