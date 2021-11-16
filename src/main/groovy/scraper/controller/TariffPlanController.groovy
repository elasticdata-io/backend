package scraper.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scraper.dto.model.UserTariffPlanSubscriptionDto
import scraper.model.TariffPlan
import scraper.service.TariffPlanService

@RestController
@RequestMapping("/tariff-plan")
class TariffPlanController {

    @Autowired
    TariffPlanService tariffPlanService

    @GetMapping()
    List<TariffPlan> list() {
       return tariffPlanService.getAll()
    }

    @PostMapping('/subscribe')
    void subscribeUser(@RequestBody UserTariffPlanSubscriptionDto dto) {
        tariffPlanService.subscribe(dto)
    }

}
