package scraper.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document
import scraper.model.types.TariffPlanConfiguration

@Document(collection = "tariff-plan")
class TariffPlan {
    @Id public String id
    @Version Long version

    public String title
    public String key
    public Number priceUsd
    public TariffPlanConfiguration configuration

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @CreatedDate
    public Date createdOnUtc
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    @LastModifiedDate
    public Date modifiedOnUtc
}
