package scraper.dto.model.task

import com.github.fge.jsonpatch.JsonPatchOperation

class PipelineRunDto {
    String hookUrl
    List<JsonPatchOperation> jsonCommandsPatch
    Boolean withoutDependencies
}
