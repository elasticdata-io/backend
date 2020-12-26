package scraper.service.dto.mapper.pipeline

import scraper.service.dto.model.pipeline.BrowserWindowDslDto
import scraper.service.dto.model.pipeline.DataRuleDslDto
import scraper.service.dto.model.pipeline.DslDto
import scraper.service.dto.model.pipeline.SettingsDslDto
import scraper.service.dto.model.pipeline.UserInteractionDsl
import scraper.service.model.types.PipelineDsl
import scraper.service.model.types.SettingsDsl
import scraper.service.model.types.UserInteractionDslSettings
import scraper.service.model.types.BrowserWindowDsl

class PipelineDslMapper {

    static PipelineDsl toPipelineDsl(DslDto pipelineDslDto) {
        if (!pipelineDslDto) {
            return null
        }
        def settings = pipelineDslDto.settings
        def dsl = new PipelineDsl()
        dsl.commands = pipelineDslDto.commands
        dsl.dataRules = pipelineDslDto.dataRules
        dsl.version = pipelineDslDto.version
        def settingsDsl = new SettingsDsl()
        settingsDsl.proxies = settings?.proxies
        settingsDsl.maxWorkingMinutes = settings?.maxWorkingMinutes
                ? settings.maxWorkingMinutes
                : 24  * 60
        def window = settings?.window
        if (window) {
            settingsDsl.window = new BrowserWindowDsl(
                height: window?.height,
                width: window?.width,
                lang: window?.lang,
            )
        }
        if (settings?.userInteraction?.watchCommands) {
            settingsDsl.userInteraction = new UserInteractionDslSettings(
                watchCommands: settings?.userInteraction?.watchCommands
            )
        }
        settingsDsl.needProxyRotation = pipelineDslDto?.settings?.needProxyRotation
        dsl.settings = settingsDsl
        return dsl
    }

    static DslDto toPipelineDslDto(PipelineDsl pipelineDsl) {
        if (!pipelineDsl) {
            return null
        }
        def settings = pipelineDsl?.settings
        def dto = new DslDto()
        dto.commands = pipelineDsl.commands as List<HashMap>
        dto.dataRules = pipelineDsl.dataRules as List<DataRuleDslDto>
        dto.version = pipelineDsl.version
        def settingsDto = new SettingsDslDto()
        settingsDto.proxies = settings?.proxies
        settingsDto.maxWorkingMinutes = settings?.maxWorkingMinutes
        if (settings?.window) {
            settingsDto.window = new BrowserWindowDslDto(
                lang: settings?.window?.lang,
                width: settings?.window?.width,
                height: settings?.window?.height,
            )
        }
        if (settings?.userInteraction?.watchCommands) {
            settingsDto.userInteraction = new UserInteractionDsl(
                watchCommands: settings.userInteraction.watchCommands,
            )
        }
        settingsDto.needProxyRotation = settings?.needProxyRotation
        dto.settings = settingsDto
        return dto
    }
}
