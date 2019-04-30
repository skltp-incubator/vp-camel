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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.integrationtests.utils.TakMockWebService;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

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
    }

    @Test
    public void setCorrelationAndConsumerIdTestNoMembers() throws Exception {
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeader("x-rivta-original-serviceconsumer-hsaid").equals("tp"));
        if (propagateCorrIdForHttps) {
            String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-skltp-correlation-id");
            assertNotNull(s);
            assert(!s.equals("aTestCorrelationId"));
            assert(s.length() > 20);
        } else {
            assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-skltp-correlation-id"));
        }
    }

    @Test
    public void setCorrelationAndConsumerIdTest() throws Exception {
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-rivta-original-serviceconsumer-hsaid").equals("aTestConsumer"));
        if (propagateCorrIdForHttps) {
            assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-skltp-correlation-id").equals("aTestCorrelationId"));
        } else {
            assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-skltp-correlation-id"));
        }
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
