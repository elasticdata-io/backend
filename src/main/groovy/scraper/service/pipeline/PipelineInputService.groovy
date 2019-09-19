package scraper.service.pipeline

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import scraper.core.command.input.UserInput
import scraper.core.command.input.ExternalInputCommand
import scraper.core.pipeline.PipelineProcess

@Service
class PipelineInputService {

    @Autowired
    PipelineService pipelineService

    @Autowired
    private ApplicationContext appContext

    UserInput findUserInput(String pipelineId, String userInputKey) {
        PipelineProcess pipelineProcess = pipelineService.getPipelineProcessBeanById(pipelineId)
        if (!pipelineProcess) {
            return
        }
        ExternalInputCommand externalInputCommand = pipelineProcess.commands.find { command ->
            if (command instanceof ExternalInputCommand) {
                def externalInputCommand = command as ExternalInputCommand
                UserInput userInput = externalInputCommand.userInput
                return userInput?.key == userInputKey
            }
        } as ExternalInputCommand
        return externalInputCommand?.userInput
    }

    List<UserInput> findUserInputs(String pipelineId) {
        PipelineProcess pipelineProcess = pipelineService.getPipelineProcessBeanById(pipelineId)
        if (!pipelineProcess) {
            return
        }
        List<UserInput> userInputs = new ArrayList<>()
        pipelineProcess.commands.each {command ->
            if (command instanceof ExternalInputCommand) {
                def externalInputCommand = command as ExternalInputCommand
                def userInput = externalInputCommand?.userInput
                if (userInput) {
                    userInputs.add(userInput)
                }
            }
        }
        return userInputs
    }
}
