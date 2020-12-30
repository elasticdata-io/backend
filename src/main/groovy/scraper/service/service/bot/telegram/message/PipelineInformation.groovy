package scraper.service.service.bot.telegram.message

import groovy.text.SimpleTemplateEngine
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import scraper.service.model.Pipeline
import scraper.service.model.Task

@Component
class PipelineInformation extends AbstractMessage {

    SendMessage getMessage(Pipeline pipeline, Task task) {
        def text = readMarkdown('en/pipeline-information.md')
        def engine = new SimpleTemplateEngine()
        String docsUrl = task?.docsUrl
        String link = "https://app.elasticdata.io/#/pipeline/edit/${pipeline.id}"
        def template = engine
                .createTemplate(text)
                .make([
                    link: link,
                    docsUrl: docsUrl,
                    *:extractProperties(pipeline)
                ])
        SendMessage message = new SendMessage()
        def body = template.toString().stripMargin()
        message.setText(body)
        message.setParseMode('Markdown')
        return message
    }
}
