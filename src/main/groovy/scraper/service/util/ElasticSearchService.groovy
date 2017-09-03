package scraper.service.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

@Service
class ElasticSearchService {

    Logger logger = LogManager.getRootLogger()

    @Value('${elasticsearch.host}')
    String host

    @Value('${elasticsearch.port}')
    Integer port

    TransportClient client

    @PostConstruct
    void init () {
        try {
            Settings settings = Settings.builder()
                    .put("client.transport.ping_timeout", "15s")
                    .put("client.transport.ignore_cluster_name", true)
                    .put("client.transport.sniff", false)
                    .build()
            def address = InetAddress.getByName(host)
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(address, port))
        } catch (all) {
            logger.error(all)
        }
    }

    void bulk(List<HashMap<String, String>> list, String index, String type) {
        if (!client) {
            return
        }
        BulkRequestBuilder bulkRequest = client.prepareBulk()
        list.each { data ->
            bulkRequest.add(client.prepareIndex(index, type).setSource(data))
        }
        BulkResponse bulkResponse = bulkRequest.get()
        if (bulkResponse.hasFailures()) {
            logger.error(bulkResponse.buildFailureMessage())
        }
    }
}
