package se.skl.tp.vp.integrationtests.httpheader;

import java.io.IOException;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.httpheader.HeaderConfigurationProcessorImpl;
import se.skl.tp.vp.integrationtests.utils.TakMockWebService;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

import static se.skl.tp.vp.constants.HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID;
import static se.skl.tp.vp.constants.HttpHeaders.X_SKLTP_CORRELATION_ID;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HttpsHeadersIT extends CamelTestSupport {

    public static TakMockWebService takMockWebService;

    @Value("${" + PropertyConstants.PROPAGATE_CORRELATION_ID_FOR_HTTPS + "}")
    private Boolean propagateCorrIdForHttps;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @BeforeClass
    public static void beforeClass() throws IOException {
        //TODO Use dynamic ports and also set TAK address used by takcache (Override "takcache.endpoint.address" property)
        takMockWebService = new TakMockWebService("http://localhost:8086/tak-services/SokVagvalsInfo/v2");
        takMockWebService.start();
    }

    @AfterClass
    public static void afterClass() {
        takMockWebService.stop();
    }
    @Autowired
    private HeaderConfigurationProcessorImpl headerConfigurationProcessor;

    private boolean oldCorrelation;

    @Autowired
    private CamelContext camelContext;

    private static boolean isContextStarted = false;

    @Before
    public void setUp() throws Exception {
        if(!isContextStarted){
            addConsumerRoute(camelContext);
            camelContext.start();
            isContextStarted=true;
        }
        resultEndpoint.reset();
        oldCorrelation = headerConfigurationProcessor.getPropagate();
    }

    @After
    public void after() {
        headerConfigurationProcessor.setPropagate(oldCorrelation);
    }

    @Test
    public void setCorrelationAndConsumerIdNoMembersPassCorrolationTrueTest() {
        headerConfigurationProcessor.setPropagate(true);
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID).equals("tp"));
        String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_SKLTP_CORRELATION_ID);
        assertNotNull(s);
        assert(!s.equals("aTestCorrelationId"));
        assert(s.length() > 20);
    }

    @Test
    public void setCorrelationAndConsumerIdNoMembersPassCorrolationFalseTest() throws Exception {
        headerConfigurationProcessor.setPropagate(false);
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID).equals("tp"));
        assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_SKLTP_CORRELATION_ID));
    }

    @Test
    public void setCorrelationAndConsumerIdPassCorrolationTrueTest() throws Exception {
        headerConfigurationProcessor.setPropagate(true);
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID).equals("aTestConsumer"));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_SKLTP_CORRELATION_ID).equals("aTestCorrelationId"));
    }

    @Test
    public void setCorrelationAndConsumerIdPassCorrolationFalseTest() throws Exception {
        headerConfigurationProcessor.setPropagate(false);
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID).equals("aTestConsumer"));
        assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_SKLTP_CORRELATION_ID));
    }

    private void addConsumerRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(
        new RouteBuilder() {
          @Override
          public void configure() {
            from("direct:start").routeId("start")
                .to("netty4-http:https://localhost:1024/vp?sslContextParameters=#incomingSSLContextParameters&ssl=true&" +
                        "sslClientCertHeaders=true&needClientAuth=true&matchOnUriPrefix=true");

            from("netty4-http:https://localhost:19000/vardgivare-b/tjanst2?sslContextParameters=#outgoingSSLContextParameters&ssl=true")
                .to("mock:result");
          }
        });
    }
}
