package scraper.service.model.mapper

import groovy.json.JsonSlurper
import org.springframework.stereotype.Component
import scraper.service.model.types.PipelineConfiguration
import scraper.service.model.types.PipelineSettings
import scraper.service.model.types.PipelineWindowSettings

@Component
class PipelineConfigurationMapper {

    static PipelineConfiguration toPipelineConfiguration(String pipelineJson) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        HashMap pipelineMap = jsonSlurper.parseText(pipelineJson) as HashMap
        HashMap settingsMap = pipelineMap.get('settings', new HashMap())
        def pipelineConfiguration = new PipelineConfiguration()
        pipelineConfiguration.commands = pipelineMap.get('commands')
        pipelineConfiguration.dataRules = pipelineMap.get('dataRules')
        pipelineConfiguration.version = pipelineMap.get('version')
        def pipelineSettings = new PipelineSettings()
        pipelineSettings.proxies = settingsMap.get('proxies', [])
        pipelineSettings.maxWorkingMinutes = settingsMap.get('maxWorkingMinutes', 24  * 60)
        def window = settingsMap.get('window', new HashMap<>()) as HashMap
        if (!window.isEmpty()) {
            pipelineSettings.window = new PipelineWindowSettings(
                height: window.get('height'),
                width: window.get('width'),
                lang: window.get('lang'),
            )
        }
        pipelineConfiguration.settings = pipelineSettings
        return pipelineConfiguration
    }
}
