package pro.ra_tech.giga_ai_agent.core;

import com.pgvector.PGvector;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.EnqueueDocumentRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.EnqueueDocumentResponse;
import pro.ra_tech.giga_ai_agent.core.util.Constants;
import pro.ra_tech.giga_ai_agent.core.util.TestUtils;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;
import pro.ra_tech.giga_ai_agent.integration.config.giga.GigaChatProps;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = 10035)
@TestPropertySource(properties = "mockServerPort=10035")
class DocumentsIT extends AbstractApiIT {
    private static final String DOCUMENTS_API_PATH = "/api/v1/documents";
    private static final List<Double> TEST_VECTOR = List.of(0.49975586,-1.1982422,-0.3034668,-0.46313477,1.0927734,-0.5209961,-0.7763672,2.9199219,1.0351562,-0.38134766,0.50341797,0.64746094,-1.0263672,-0.75146484,-1.046875,-0.16442871,-0.18579102,0.67333984,-0.4638672,-0.27954102,1.7148438,-0.2467041,-1.3867188,-0.7475586,-0.5839844,-0.9819336,-0.57470703,-1.5302734,-0.46850586,-1.3037109,-0.13623047,0.018310547,-0.9379883,-0.54003906,-0.7651367,0.4645996,0.61279297,0.5649414,-0.45263672,0.80566406,0.008171082,1.3164062,-0.09588623,-1.1191406,0.058654785,0.32885742,0.9345703,-0.80078125,0.16040039,0.80908203,1.1875,1.0419922,-0.20947266,0.08013916,-1.2197266,1.3427734,-0.375,0.16442871,-1.5166016,0.7529297,-0.2578125,-0.8642578,-0.115112305,-1.1347656,0.1895752,0.8183594,0.63623047,0.6621094,-0.98876953,0.36743164,-0.7890625,0.036102295,-0.50390625,-0.4321289,-0.7495117,-0.09100342,0.57910156,-0.86035156,0.54785156,-0.06542969,1.3203125,-0.18762207,0.7011719,-0.95214844,0.5805664,0.5913086,0.42749023,0.83984375,0.87353516,0.7216797,-0.83251953,0.9760742,1.3115234,-0.011299133,-1.0048828,0.85302734,0.92333984,1.0351562,-0.6669922,-0.052246094,-0.115600586,-0.8852539,0.13671875,-0.9501953,-0.1227417,0.9316406,0.6933594,0.95458984,0.63134766,-0.06970215,1.0488281,1.2861328,0.07904053,-1.1171875,-0.88183594,0.26831055,-0.42651367,-0.53808594,-0.19250488,0.89501953,0.8100586,1.0273438,-1.1455078,-1.9384766,1.1259766,-0.009979248,0.5805664,1.1396484,-0.7519531,-0.17883301,1.2597656,1.4111328,-0.9165039,-0.30737305,-1.4384766,-0.21777344,-0.46533203,-0.6298828,-0.6640625,0.20117188,-0.061401367,1.1738281,1.6943359,-0.7451172,-1.1630859,-1.5380859,-0.8076172,0.17858887,-0.15258789,-0.5395508,-0.31225586,-0.32128906,-1.1425781,1.2802734,1.4599609,-0.51708984,0.3647461,1.4707031,1.0664062,0.2006836,-0.9399414,-1.0576172,-0.30493164,-0.4104004,-0.27294922,-0.703125,0.051757812,0.24780273,-0.26342773,-0.89941406,-0.85302734,-1.3632812,0.044036865,-1.0703125,-0.56933594,0.73828125,0.17553711,1.0019531,1.2646484,-0.7915039,0.75927734,0.84472656,1.3818359,-0.66064453,-0.20251465,0.6586914,1.3125,0.7939453,0.93652344,1.2460938,0.70654297,-0.90771484,0.6533203,1.0048828,-0.8076172,1.4208984,0.8251953,1.21875,-0.036987305,-2.0351562,0.41992188,1.2548828,-1.1113281,0.22277832,0.94140625,-0.7807617,-1.4443359,-1.1279297,1.0146484,-1.4658203,-1.7529297,-1.0673828,0.95166016,0.46484375,-1.3642578,-1.453125,-1.5673828,-0.0670166,-0.012229919,-0.11291504,0.4013672,2.0996094,-0.26660156,1.3300781,0.34765625,0.83740234,0.8725586,0.0026226044,1.3798828,0.7109375,0.12573242,0.10406494,-1.7685547,-1.0410156,0.014770508,-0.9433594,-0.31176758,-0.78125,0.7368164,1.2529297,-0.44604492,0.21313477,0.1303711,-0.26293945,-0.66259766,0.25952148,0.13208008,-0.65527344,0.69873047,-0.22033691,0.6533203,0.23718262,-0.62646484,-0.5649414,1.8945312,-0.026062012,0.9580078,-0.5654297,-0.43017578,-0.42773438,-0.011985779,-0.07232666,-0.35058594,1.0976562,1.2880859,0.67578125,-0.079833984,-0.44970703,-0.9458008,-0.3869629,-1.4023438,-0.890625,-0.7163086,-0.7104492,-0.64746094,0.36743164,-1.3798828,-1.25,-0.10736084,-0.1418457,-0.00068092346,-0.30078125,1.4609375,-0.8222656,0.05609131,0.31103516,0.63623047,-0.26904297,0.35742188,0.4580078,0.6088867,-0.6166992,2.9570312,0.38183594,0.09466553,0.40478516,-1.9326172,-2.8964844,-0.62402344,0.5908203,-0.55029297,-0.84277344,0.8881836,0.48876953,-0.3005371,-0.48754883,-0.03314209,0.7265625,-1.7011719,0.15515137,1.390625,0.80566406,1.2900391,-1.4707031,-1.8740234,0.14282227,-0.34838867,1.0361328,-0.4958496,1.1123047,-1.4248047,0.39990234,-0.22741699,0.39624023,0.45654297,0.3466797,0.19238281,0.027359009,-0.36108398,-0.92333984,0.9892578,-0.11682129,0.004535675,2.2910156,-0.34350586,1.0517578,-1.5058594,0.3540039,0.85253906,0.20336914,0.4724121,0.3527832,-1.3701172,-0.02507019,0.2524414,-1.2011719,-0.03225708,-0.8833008,0.46020508,0.49194336,-0.7807617,-0.17248535,0.7607422,-0.010551453,-0.80908203,1.7539062,0.61279297,-1.1376953,1.3056641,-0.5410156,0.8964844,0.51220703,1.4121094,-0.24291992,-0.4013672,-0.5234375,0.2319336,-0.050628662,-1.3232422,-1.1367188,-0.07739258,-0.009643555,-0.10272217,-0.1484375,0.55615234,0.2705078,-1.9619141,-0.7104492,-0.6303711,-0.9189453,-0.8046875,-0.6972656,0.87353516,-0.5185547,-0.042785645,-0.6230469,5.9140625,-0.55859375,1.4726562,-0.7373047,0.24169922,-0.3935547,-0.48706055,1.2636719,0.6713867,1.0996094,-0.19042969,0.86621094,0.85498047,-0.23120117,0.515625,0.8486328,-0.076416016,0.5102539,0.78564453,-0.42651367,2.1328125,-0.91503906,-0.23291016,1.4482422,-1.6972656,-1.0058594,0.85009766,1.2636719,-1.7265625,0.19689941,-1.0126953,0.7558594,-0.62353516,-0.32226562,0.45385742,0.8383789,0.43530273,-0.34643555,1.2441406,-0.066345215,-0.89453125,0.037200928,-1.3408203,0.3005371,-0.5541992,0.98828125,1.6503906,-0.30664062,-1.1503906,0.35961914,0.3347168,1.0498047,-0.58935547,-1.2617188,0.06738281,-1.2626953,-0.5541992,-0.35083008,0.62109375,0.80859375,1.5253906,-0.9511719,-1.0859375,-0.2697754,1.2050781,0.53515625,-0.66748047,-1.9550781,-0.6582031,-1.4570312,-0.90722656,0.1060791,-0.91748047,0.79003906,-0.010475159,-0.32226562,-0.82958984,2.3964844,0.5019531,-0.49414062,0.96875,-0.36938477,-0.6152344,0.5786133,1.5625,-0.37426758,0.29418945,0.92089844,-0.9824219,0.4975586,0.44262695,0.53564453,-0.61083984,1.2275391,-0.24511719,-1.2070312,-0.25952148,0.33569336,-0.7758789,-0.9189453,-0.7216797,0.045410156,-0.77246094,-0.59375,-1.1914062,-0.9326172,0.52783203,0.7055664,0.796875,-0.5161133,-0.24267578,0.5620117,0.111816406,-0.5410156,1.3730469,0.041290283,0.9975586,-0.91308594,1.0664062,0.35229492,0.023284912,1.2333984,-0.56933594,1.6542969,-0.58740234,1.1640625,-0.8198242,0.052368164,0.5727539,-0.08874512,0.44384766,-0.6933594,-0.8066406,0.90966797,-0.38745117,-0.6411133,1.6699219,-1.1884766,-0.0519104,1.7841797,-0.9511719,1.6025391,-1.1630859,0.61035156,0.14733887,0.35180664,0.46850586,-1.1806641,-0.39794922,-1.3613281,0.38623047,-0.35375977,0.79833984,0.47460938,-0.2548828,-1.5429688,0.8725586,0.95654297,1.7929688,-0.421875,0.23937988,-0.13183594,0.6508789,3.0117188,2.1738281,1.3730469,-0.140625,1.3945312,0.40820312,0.55126953,-0.37939453,-0.4033203,0.18457031,1.4658203,0.5136719,-1.1787109,0.036376953,0.13110352,-0.51708984,-0.08972168,0.053894043,-0.055755615,0.3474121,-0.45483398,-0.22814941,-0.55859375,-0.20922852,-0.95996094,-0.23730469,0.011077881,-0.8876953,3.3046875,-0.86035156,-0.19812012,-0.8339844,-0.20715332,0.9472656,0.27856445,-1.0908203,-0.97558594,-0.19848633,-1.4902344,0.19262695,0.04949951,-0.7055664,-0.09313965,-0.16271973,0.29418945,0.9692383,1.0771484,-0.13513184,0.24438477,0.11468506,0.44506836,0.38745117,1.2880859,1.4785156,1.0634766,0.8569336,-0.76171875,0.9477539,-0.2121582,0.4345703,-0.4970703,-1.3808594,-0.50146484,0.71875,0.13989258,0.17272949,-0.98828125,0.8203125,-0.93603516,-0.56396484,-0.6777344,0.051574707,0.41088867,1.7490234,0.265625,0.11730957,-0.6274414,0.7636719,-0.31835938,0.60791016,-1.2148438,-1.0205078,0.38476562,-1.2294922,-0.36254883,0.9785156,0.7001953,0.5571289,-1.4345703,1.0185547,1.0654297,-1.0722656,-0.84472656,-0.1427002,-0.75634766,-1.3105469,-1.4033203,0.49316406,-0.28344727,-0.5595703,-0.50390625,-0.93408203,1.1269531,0.92626953,0.21057129,-0.17443848,-0.84375,-0.359375,-1.3808594,-1.0341797,-2.4960938,1.4287109,0.45385742,0.15283203,-1.2597656,-0.65283203,-0.95458984,-0.9477539,1.8457031,0.31054688,-0.26489258,0.56591797,0.009590149,-0.4584961,0.93408203,-0.6933594,-1.5712891,-1.0341797,1.1552734,-0.6816406,0.30908203,-0.5131836,-1.1845703,-0.8461914,-0.23132324,-0.6298828,-0.4777832,0.43408203,-0.25732422,-0.85498047,-0.46850586,-1.0830078,0.8491211,-0.7426758,-0.71191406,1.1552734,0.87109375,-0.4387207,-1.3349609,-0.14672852,1.8867188,-0.024002075,0.07299805,0.8881836,-0.9663086,-0.7792969,1.2939453,-0.86328125,-0.7651367,-0.5083008,0.5390625,1.4414062,0.18920898,1.3144531,0.31274414,0.6899414,0.5292969,-0.65527344,-0.43823242,-0.86572266,-0.1496582,-1.5224609,-1.2226562,-0.66015625,-0.67285156,-0.023208618,-1.2099609,-0.7495117,0.042175293,0.8833008,-0.26782227,1.1484375,0.42529297,-0.25341797,0.68408203,-0.44702148,-0.56103516,-0.5566406,0.98291016,-0.48217773,0.26220703,-1.0537109,1.9130859,-1.6826172,1.5351562,0.41455078,-1.5390625,-0.27294922,0.5966797,0.1463623,-0.7988281,-0.81396484,-0.058654785,-0.4013672,0.31396484,-1.8974609,-0.101135254,-0.34960938,-0.26660156,-1.1630859,-0.5566406,-0.78564453,0.34570312,-1.0537109,-0.113220215,0.9707031,0.03717041,0.26171875,-1.5966797,-0.78271484,-0.45532227,-0.546875,-1.0273438,-0.1194458,0.95214844,0.56152344,1.2275391,-1.0996094,1.4814453,-0.8310547,-0.09643555,-2.6738281,0.29516602,-0.11077881,-0.6557617,-1.0371094,-1.0644531,-2.25,0.18041992,-0.057800293,-0.9980469,0.024337769,0.57421875,0.44262695,-0.041107178,-0.48046875,1.4726562,1.5439453,0.11230469,0.08654785,-0.38623047,0.9433594,0.24108887,-0.35229492,-0.046325684,1.7207031,0.9926758,1.1650391,0.33691406,-1.6533203,-1.390625,-0.19970703,-0.5957031,-0.046569824,0.9995117,-0.9248047,0.6557617,-0.5727539,-0.28735352,0.06378174,0.5444336,-0.7133789,0.31298828,-0.8149414,0.40356445,-0.08062744,0.8852539,0.43164062,-0.6069336,0.71533203,-1.1650391,-0.9291992,-0.36523438,0.54248047,1.7451172,0.6254883,0.6035156,0.78125,-0.47021484,-0.045196533,1.7890625,-0.3166504,-0.3618164,-0.9863281,-0.96972656,-0.6328125,-0.47338867,-1.2861328,-0.8823242,-0.3005371,-0.08062744,0.047698975,-0.8774414,-0.033447266,-0.79345703,0.4645996,-0.13305664,-0.5390625,0.32543945,1.3691406,1.0263672,-0.15966797,-0.49560547,-0.03237915,0.1496582,0.04650879,0.40039062,-0.19189453,-0.53808594,-1.6962891,0.3720703,-1.3017578,0.00039625168,0.6088867,-1.1171875,-0.13671875,-0.22485352,-1.4697266,-0.6665039,-0.09509277,1.2255859,-0.24938965,-0.39916992,1.0820312,0.4140625,0.3190918,-0.117614746,0.62060547,-0.21118164,1.3974609,-0.6845703,-1.1884766,-0.64160156,-0.40551758,0.13647461,-0.8222656,0.2479248,0.33691406,0.34838867,-1.0908203,0.10546875,-1.0019531,-1.9003906,1.5087891,-0.6928711,0.15478516,-1.1132812,-0.10534668,-1.7060547,0.019622803,-1.3242188,2.1210938,-1.3994141,0.8754883,-0.2602539,0.07696533,-0.5800781,-0.46069336,2.3613281,-0.29370117,1.0361328,-1.1259766,-1.2421875,-1.2939453,-0.25341797,-0.6948242,0.07147217,-1.171875,-1.1572266,-0.70996094,-0.9399414,0.46850586,-0.55371094,-1.0498047,1.3320312,-0.15551758,0.3840332,0.055389404,-0.41455078,0.33544922,0.76464844,-0.37304688,0.41918945,0.18371582,1.1044922,0.078186035,-2.1777344,-0.124694824,0.52978516,-0.78515625,0.49243164,1.0732422,0.46020508,-0.96875,0.8149414,0.33154297,-0.10638428,1.1230469,0.028518677,0.87939453,0.3852539,0.58447266,0.052459717,0.5625,0.59033203,-0.03201294,1.2138672,0.48657227,0.73876953,-0.51464844,0.40478516,-0.72314453,0.5229492,-1.0078125,0.99609375,0.84375,-1.5097656,-0.18017578,0.15808105,-0.28100586,-0.073913574,-0.5991211,-0.7915039,0.99609375,-0.82421875,-1.3955078,-0.5727539,-0.91259766,-0.37573242,-0.6464844,1.3085938,1.2792969,0.32348633,-0.45385742,0.43774414,1.1787109,0.83984375,1.4423828,-0.5004883,0.33129883,0.63623047,0.64501953,0.5522461,-0.09301758,0.035125732,-0.19567871,0.3786621,-0.50683594,0.86035156,-0.6298828,-1.0976562,-0.17041016,0.19128418,0.118896484,0.66308594,0.15258789,-0.9423828,0.5004883,0.049591064,0.359375,0.7368164,-0.25024414,-0.44726562,-0.41235352,0.3803711);

    @Container
    private static final PostgreSQLContainer pgContainer = new PostgreSQLContainer(
            DockerImageName.parse(Constants.PG_VECTOR_DOCKER_IMAGE_NAME)
    )
            .withDatabaseName("giga_ai_agent")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-db.sql")
            .withExposedPorts(Constants.PG_VECTOR_DOCKER_PORT);

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    )
            .withExposedPorts(9092);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl);
        registry.add("app.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    static {
        Startables.deepStart(pgContainer).join();
        Startables.deepStart(kafkaContainer).join();
    }

    @Autowired
    private DocProcessingTaskRepository taskRepo;
    @Autowired
    private JdbcClient jdbc;
    @Autowired
    private GigaChatProps gigaChatProps;

    @BeforeEach
    void beforeEach() {
        jdbc.sql("DELETE FROM embeddings").update();
    }

    private void setupLlmTextProcessor(ClientAndServer client) {
        client.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/chunkers/recursive/split-text")
                        .withBody(JsonBody.json(TestUtils.readResourceFileAsString("mockserver/split-text-request.json")))
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(TestUtils.readResourceFileAsString("mockserver/split-text-response.json"))
        );
    }

    private void setupHfs(ClientAndServer client, byte[] data) {
        // Upload
        client.when(
                request()
                        .withMethod("PUT")
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_OCTET_STREAM)
                        .withPath("/hfs-test/" + Constants.UUID_REGEX)
        ).respond(
                response()
                        .withStatusCode(200)
        );

        // Download
        client.when(
                request()
                        .withMethod("GET")
                        .withPath("/hfs-test/" + Constants.UUID_REGEX)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_OCTET_STREAM)
                        .withBody(data)
        );

        // Comment
        client.when(
                request()
                        .withMethod("POST")
                        .withPath("/~/api/comment")
                        .withBody(TestUtils.readResourceFileAsString("mockserver/hfs-comment-request.json"))
        ).respond(
                response()
                        .withStatusCode(200)
        );
    }

    private void setupGigaApi(ClientAndServer client) {
        val exp = OffsetDateTime.now().plus(Duration.ofHours(1)).toEpochSecond()*1000;

        client.when(
                request()
                        .withMethod("POST")
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_FORM_URLENCODED)
                        .withPath("/api/v2/oauth")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody("{ \"access_token\": \"TOP_SECRET_TOKEN\", \"expires_at\": " + exp + " }")
        );

        // Embedding 1
        for (int i = 1; i <= 3; ++i) {
            client.when(
                    request()
                            .withMethod("POST")
                            .withPath("/api/v1/embeddings")
                            .withBody(JsonBody.json(TestUtils.readResourceFileAsString("mockserver/create-embeddings-request-" + i + ".json")))
            ).respond(
                    response()
                            .withStatusCode(200)
                            .withBody(TestUtils.readResourceFileAsString("mockserver/create-embeddings-response-" + i + ".json"))
            );
        }
    }

    private boolean isDocumentTaskFinished(long taskId) {
        return taskRepo.findById(taskId).get().status() == DocProcessingTaskStatus.SUCCESS;
    }

    @SneakyThrows
    private PGvector toPgVector(String value) {
        val vector = new PGvector();
        vector.setValue(value);

        return vector;
    }

    private List<List<Double>> readEmbeddings() {
        return jdbc.sql("SELECT vector_data FROM embeddings")
                .query()
                .listOfRows()
                .stream()
                .map(row -> row.get("vector_data"))
                .filter(Objects::nonNull)
                .map(Objects::toString)
                .map(this::toPgVector)
                .peek(vector -> log.info("Read vector: {}", vector))
                .map(PGvector::toArray)
                .filter(Objects::nonNull)
                .map(floats -> IntStream.range(0, floats.length).mapToDouble(i -> floats[i]).boxed().toList())
                .toList();
    }

    @Test
    void shouldProcessTextDocumentAsync(@Autowired RestTestClient rest, ClientAndServer mockServerClient) {
        val fileName = "TestDocument.txt";
        val fileBytes = TestUtils.readResourcesFileBytes(fileName);

        log.info("File bytes: {}, {}, {}, size: {}", fileBytes[0], fileBytes[1], fileBytes[2], fileBytes.length);

        setupLlmTextProcessor(mockServerClient);
        setupHfs(mockServerClient, fileBytes);
        setupGigaApi(mockServerClient);

        val body = new LinkedMultiValueMap<>();
        val fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<Resource> filePart = new HttpEntity<>(fileResource, httpHeaders);
        body.add("file", filePart);

        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(
                new EnqueueDocumentRequest(
                        "TestDocument",
                        List.of("test"),
                        "Text document description"
                ),
                httpHeaders
        );
        body.add("metadata", request);

        Awaitility.setDefaultPollInterval(100, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(Duration.ofSeconds(30));

        rest.post()
                .uri(DOCUMENTS_API_PATH + "/enqueue")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnqueueDocumentResponse.class)
                .consumeWith(result -> {
                    val res = result.getResponseBody();
                    assertThat(res).isNotNull().hasOnlyFields("taskId", "hfsFileName");
                    assertThat(res.taskId()).isPositive();
                    assertThat(res.hfsFileName()).isNotEmpty();

                    await().until(() -> isDocumentTaskFinished(res.taskId()));

                    val vectors = readEmbeddings();
                    assertThat(vectors.size()).isEqualTo(3);
                    vectors.forEach(vector -> {
                        for (int i = 0; i < vector.size(); ++i) {
                            assertThat(Math.abs(vector.get(i) - TEST_VECTOR.get(i))).isLessThan(0.0001);
                        }
                    });
                });
    }
}
