package scraper.service.dto.model.pipeline

class DslDto {
    List<HashMap> commands
    SettingsDslDto settings
    List<DataRuleDslDto> dataRules
    String version
}
