package scraper.service.service

import com.github.fge.jsonpatch.JsonPatch
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonBuilder
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.springframework.stereotype.Service

@Service
class PatchService {

    private static Logger logger = LogManager.getLogger(PatchService.class)

    public  <T> T patch(JsonPatch patch, T targetBean, Class<T> beanClass) {
        ObjectMapper mapper = new ObjectMapper()
        HashMap map = mapper.convertValue(targetBean, HashMap.class)
        String json = new JsonBuilder(map).toPrettyString()
        JsonNode node = mapper.readTree(json)
        def patched = patch.apply(node)
        return mapper.convertValue(patched, beanClass)
    }

}
