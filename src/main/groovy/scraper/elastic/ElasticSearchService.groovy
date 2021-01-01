package scraper.elastic

import groovy.json.JsonBuilder
import org.apache.http.HttpHost
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.common.xcontent.XContentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Service
class ElasticSearchService {

    Logger logger = LogManager.getRootLogger()

    @Value('${elasticsearch.host}')
    String host

    @Value('${elasticsearch.httpPort}')
    Integer port

    @Value('${elasticsearch.enabled}')
    Boolean enabled

    RestHighLevelClient client

    @PostConstruct
    void init () {
        if (!enabled) {
            return
        }
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(host, port))
        client = new RestHighLevelClient(restClientBuilder)
    }

    void bulk(List<HashMap<String, String>> list, String index, String type) {
        if (!client) {
            return
        }
        createEsIndex(index)
        BulkRequest bulkRequest = new BulkRequest()
        list.each { doc ->
            String id = getDocumentIdHash(doc)
            IndexRequest request = new IndexRequest(index)
            String json = new JsonBuilder(doc).toString()
            request.source(json, XContentType.JSON)
            request.id(id)
            bulkRequest.add(request)
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT)
        if (bulkResponse.hasFailures()) {
            logger.error(bulkResponse.buildFailureMessage())
        }
    }

    private void createEsIndex(String index) {
        if (esIndexExists(index)) {
            return
        }
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index)
        client
                .indices()
                .create(createIndexRequest, RequestOptions.DEFAULT)
    }

    private boolean esIndexExists(String index) {
        GetIndexRequest request = new GetIndexRequest(index)
        return client.indices().exists(request, RequestOptions.DEFAULT)
    }

    private String getDocumentIdHash(Map<String, String> doc) {
        String all = ''
        doc.each{ key, value ->
            all += "${key}${value}"
        }
        return all.md5()
    }
}
