package scraper.service.controller

import com.cloudinary.Cloudinary
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import groovy.time.TimeCategory
import scraper.service.model.PipelineTask
import scraper.service.repository.PipelineRepository
import scraper.service.repository.PipelineTaskRepository

@RestController
@RequestMapping("/api/vk")
class VkontakteController {

    @Autowired
    PipelineRepository pipelineRepository;

    @Autowired
    PipelineTaskRepository pipelineTaskRepository;

    @Autowired
    PipelineController pipelineController;

    /**
     * Runs pipeline process by pipeline id.
     * @param id
     * @throws UnknownHostException
     */
    @RequestMapping("/last-messages")
    void sendToSlack() throws UnknownHostException {
        def today = new Date();
        use(TimeCategory) {
            def from = today - 1.hour
            PipelineTask pipelineTask = pipelineTaskRepository.findByEndOnBetween(from, today);
            if (!pipelineTask) {
                pipelineController.run("5928b48fd95579a675bbe75f");
                this.sendToSlack();
                return;
            }
            def data = pipelineTask.data as List<HashMap<String, String>>;
            def screenshot = data.first().get("screenshot");

            HashMap config = new HashMap<>();
            config.put("cloud_name", "dfefs0paw");
            config.put("api_key", "262388332231287");
            config.put("api_secret", "GAqJZ-Vkg3HOWIxBuTEAI1TdTdk");
            Cloudinary cloudinary = new Cloudinary(config);

            byte[] bytes = Base64.getDecoder().decode(screenshot);

            File file = new File("tmp.jpg");
            def fop = new FileOutputStream(file);

            fop.write(bytes);

            fop.flush();
            fop.close();

            Map uploadResult = cloudinary.uploader().upload(file, config);
            String url = uploadResult.get("url");
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpPost httppost = new HttpPost("https://hooks.slack.com/services/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");

                StringEntity reqEntity = new StringEntity("{\"text\":\"${url}\"}",
                        "application/json",
                        "UTF-8");
                httppost.setEntity(reqEntity);

                System.out.println("Executing request: " + httppost.getRequestLine());
                CloseableHttpResponse response = httpclient.execute(httppost);
                try {
                    System.out.println("----------------------------------------");
                    System.out.println(response.getStatusLine());
                    System.out.println(EntityUtils.toString(response.getEntity()));
                } finally {
                    response.close();
                }
            } finally {
                httpclient.close();
            }
        }
    }

}
