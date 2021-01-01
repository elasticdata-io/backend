package scraper.dto.mapper.pipeline


import scraper.dto.model.pipeline.BrowserWindowDslDto
import scraper.dto.model.pipeline.DslDto
import scraper.dto.model.pipeline.NetworkDslDto
import scraper.dto.model.pipeline.SettingsDslDto
import scraper.dto.model.pipeline.SkipResourcesDslDto
import scraper.dto.model.pipeline.UserInteractionDsl

class DslMapper {

    static scraper.model.types.PipelineDsl toDslEntity(DslDto dslDto) {
        if (!dslDto) {
            return null
        }
        def settings = dslDto.settings
        def dsl = new scraper.model.types.PipelineDsl()
        dsl.commands = dslDto.commands
        dsl.dataRules = dslDto.dataRules
        dsl.version = dslDto.version
        def settingsDsl = new scraper.model.types.SettingsDsl()
        if (settings?.proxies) {
            settingsDsl.proxies = settings.proxies
        }
        if (settings?.maxWorkingMinutes) {
            settingsDsl.maxWorkingMinutes = settings.maxWorkingMinutes ?: 24  * 60
        }
        def window = settings?.window
        if (window) {
            settingsDsl.window = new scraper.model.types.BrowserWindowDsl(
                height: window?.height,
                width: window?.width,
                lang: window?.lang,
            )
        }
        if (settings?.userInteraction?.watchCommands) {
            settingsDsl.userInteraction = new scraper.model.types.UserInteractionDslSettings(
                watchCommands: settings?.userInteraction?.watchCommands
            )
        }
        if (settings?.network?.skipResources) {
            settingsDsl.network = new scraper.model.types.NetworkDsl(
                skipResources: new scraper.model.types.SkipResourcesDsl(
                    stylesheet: settings.network.skipResources?.stylesheet,
                    image: settings.network.skipResources?.image,
                    font: settings.network.skipResources?.font,
                )
            )
        }
        if (dslDto?.settings?.needProxyRotation) {
            settingsDsl.needProxyRotation = dslDto.settings.needProxyRotation
        }
        dsl.settings = settingsDsl
        return dsl
    }

    static DslDto toPipelineDslDto(scraper.model.types.PipelineDsl pipelineDsl) {
        if (!pipelineDsl) {
            return null
        }
        def settings = pipelineDsl?.settings
        def dto = new DslDto()
        dto.commands = pipelineDsl.commands as List<HashMap>
        dto.dataRules = pipelineDsl.dataRules as List<Object>
        dto.version = pipelineDsl.version
        def settingsDto = new SettingsDslDto()
        if (settings?.proxies) {
            settingsDto.proxies = settings.proxies
        }
        if (settings?.maxWorkingMinutes) {
            settingsDto.maxWorkingMinutes = settings.maxWorkingMinutes
        }
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
        if (settings?.network?.skipResources) {
            settingsDto.network = new NetworkDslDto(
                skipResources: new SkipResourcesDslDto(
                    stylesheet: settings.network.skipResources?.stylesheet,
                    image: settings.network.skipResources?.image,
                    font: settings.network.skipResources?.font,
                )
            )
        }
        if (settings?.needProxyRotation) {
            settingsDto.needProxyRotation = settings.needProxyRotation
        }
        if (settingsDto) {
            dto.settings = settingsDto
        }
        return dto
    }
}
