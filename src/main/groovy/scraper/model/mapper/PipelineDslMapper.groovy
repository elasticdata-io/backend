package scraper.model.mapper

import groovy.json.JsonSlurper
import org.springframework.stereotype.Component

@Component
class PipelineDslMapper {

    static scraper.model.types.PipelineDsl toPipelineDsl(String pipelineJson) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        HashMap pipelineMap = jsonSlurper.parseText(pipelineJson) as HashMap
        HashMap settingsMap = pipelineMap.get('settings', new HashMap())
        def pipelineConfiguration = new scraper.model.types.PipelineDsl()
        pipelineConfiguration.commands = pipelineMap.get('commands') as List<Object>
        pipelineConfiguration.dataRules = pipelineMap.get('dataRules') as List<Object>
        pipelineConfiguration.version = pipelineMap.get('version')
        def pipelineSettings = new scraper.model.types.SettingsDsl()
        pipelineSettings.proxies = settingsMap.get('proxies', [])
        pipelineSettings.maxWorkingMinutes = settingsMap.get('maxWorkingMinutes', 24  * 60)
        def window = settingsMap.get('window', new HashMap<>()) as HashMap
        if (window) {
            pipelineSettings.window = new scraper.model.types.BrowserWindowDsl(
                height: window.get('height'),
                width: window.get('width'),
                lang: window.get('lang'),
            )
        }
        HashMap userInteractionMap = settingsMap?.get('userInteraction', new HashMap())
        List<HashMap> watchCommands = userInteractionMap?.get('watchCommands', [])
        if (watchCommands) {
            pipelineSettings.userInteraction = new scraper.model.types.UserInteractionDslSettings(watchCommands: watchCommands)
        }
        pipelineConfiguration.settings = pipelineSettings
        return pipelineConfiguration
    }
}
