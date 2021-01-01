package scraper.dto.mapper.pipeline


import scraper.dto.model.pipeline.BrowserWindowDslDto
import scraper.dto.model.pipeline.DslDto
import scraper.dto.model.pipeline.NetworkDslDto
import scraper.dto.model.pipeline.SettingsDslDto
import scraper.dto.model.pipeline.SkipResourcesDslDto
import scraper.dto.model.pipeline.UserInteractionDsl
import scraper.model.types.BrowserWindowDsl
import scraper.model.types.NetworkDsl
import scraper.model.types.PipelineDsl
import scraper.model.types.SettingsDsl
import scraper.model.types.SkipResourcesDsl
import scraper.model.types.UserInteractionDslSettings

class DslMapper {

    static PipelineDsl toDslEntity(DslDto dslDto) {
        if (!dslDto) {
            return null
        }
        def settings = dslDto.settings
        def dsl = new PipelineDsl()
        dsl.commands = dslDto.commands
        dsl.dataRules = dslDto.dataRules
        dsl.version = dslDto.version
        def settingsDsl = new SettingsDsl()
        if (settings?.proxies) {
            settingsDsl.proxies = settings.proxies
        }
        if (settings?.maxWorkingMinutes) {
            settingsDsl.maxWorkingMinutes = settings.maxWorkingMinutes ?: 24  * 60
        }
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
        if (settings?.network?.skipResources) {
            settingsDsl.network = new NetworkDsl(
                skipResources: new SkipResourcesDsl(
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

    static DslDto toPipelineDslDto(PipelineDsl pipelineDsl) {
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