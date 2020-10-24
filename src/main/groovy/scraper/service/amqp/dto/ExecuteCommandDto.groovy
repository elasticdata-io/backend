package scraper.service.amqp.dto

class ExecuteCommandDto {
    List<HashMap> commands
    String taskId
    String userInteractionId
    String pageContext
}
