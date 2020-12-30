package scraper.service.service.bot.telegram.message

abstract class AbstractMessage {

    protected String readMarkdown(String resourcesPath) {
        return getClass()
                .getResource("/bot/telegram/${resourcesPath}")
                .getText()
    }

    protected def extractProperties(obj) {
        obj.getClass()
                .declaredFields
                .findAll { !it.synthetic }
                .collectEntries { field ->
                    [field.name, obj."$field.name"]
                }
    }
}
