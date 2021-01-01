package scraper.model.mapper

import groovy.json.JsonSlurper
import org.springframework.stereotype.Component
import scraper.model.types.BrowserWindowDsl
import scraper.model.types.PipelineDsl
import scraper.model.types.SettingsDsl
import scraper.model.types.UserInteractionDslSettings

@Component
class PipelineDslMapper {

    static PipelineDsl toPipelineDsl(String pipelineJson) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        HashMap pipelineMap = jsonSlurper.parseText(pipelineJson) as HashMap
        HashMap settingsMap = pipelineMap.get('settings', new HashMap())
        def pipelineConfiguration = new PipelineDsl()
        pipelineConfiguration.commands = pipelineMap.get('commands') as List<Object>
        pipelineConfiguration.dataRules = pipelineMap.get('dataRules') as List<Object>
        pipelineConfiguration.version = pipelineMap.get('version')
        def pipelineSettings = new SettingsDsl()
        pipelineSettings.proxies = settingsMap.get('proxies', [])
        pipelineSettings.maxWorkingMinutes = settingsMap.get('maxWorkingMinutes', 24  * 60)
        def window = settingsMap.get('window', new HashMap<>()) as HashMap
        if (window) {
            pipelineSettings.window = new BrowserWindowDsl(
                height: window.get('height'),
                width: window.get('width'),
                lang: window.get('lang'),
            )
        }
        HashMap userInteractionMap = settingsMap?.get('userInteraction', new HashMap())
        List<HashMap> watchCommands = userInteractionMap?.get('watchCommands', [])
        if (watchCommands) {
            pipelineSettings.userInteraction = new UserInteractionDslSettings(watchCommands: watchCommands)
        }
        pipelineConfiguration.settings = pipelineSettings
        return pipelineConfiguration
    }
}
