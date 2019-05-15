package se.skl.tp.vp.integrationtests.httpheader;

import static se.skl.tp.vp.constants.HttpHeaders.SOAP_ACTION;
import static se.skl.tp.vp.constants.HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID;
import static se.skl.tp.vp.constants.HttpHeaders.X_SKLTP_CORRELATION_ID;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CONSUMER;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CORRELATION_ID;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_SENDER;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.httpheader.HeaderConfigurationProcessorImpl;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@StartTakService
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HttpsHeadersIT extends CamelTestSupport {

    @Value("${" + PropertyConstants.PROPAGATE_CORRELATION_ID_FOR_HTTPS + "}")
    private Boolean propagateCorrIdForHttps;

    @Value("${" + PropertyConstants.VP_HTTPS_ROUTE_URL + "}")
    private String httpsRoute;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

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
        oldCorrelation = headerConfigurationProcessor.getPropagateCorrelationIdForHttps();
    }

    @After
    public void after() {
        headerConfigurationProcessor.setPropagateCorrelationIdForHttps(oldCorrelation);
    }


    @Test
    public void checkSoapActionTest() {
        //This param is mandatory for the request to pass.
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
        String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(SOAP_ACTION);
        assertNotNull(s);
        assertTrue(!s.isEmpty());
    }

    //CorrelationId...passCorrelationId set to false.
    @Test //with content.
    public void setCorrelationIdPassCorrelationFalseTest() {
        headerConfigurationProcessor.setPropagateCorrelationIdForHttps(false);
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
        assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_SKLTP_CORRELATION_ID));
    }

    @Test //Without content
    public void setCorrelationIdNoMembersPassCorrelationFalseTest() {
        headerConfigurationProcessor.setPropagateCorrelationIdForHttps(false);
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
        assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_SKLTP_CORRELATION_ID));
    }

    //CorrelationId...passCorrelationId set to true.
    @Test //With content
    public void setCorrelationIdPassCorrelationTrueTest() {
        headerConfigurationProcessor.setPropagateCorrelationIdForHttps(true);
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
        assertTrue(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_SKLTP_CORRELATION_ID).equals(TEST_CORRELATION_ID));
    }

    @Test //Without content
    public void setCorrelationIdNoMembersPassCorrelationTrueTest() {
        headerConfigurationProcessor.setPropagateCorrelationIdForHttps(true);
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
        String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_SKLTP_CORRELATION_ID);
        assertNotNull(s);
        assertTrue(!s.equals(TEST_CORRELATION_ID));
        assertTrue(s.length() > 20);
    }

    //OriginalConsumerId
    @Test  //With content.
    public void setConsumerIdTest() {
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
        assertTrue(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID).equals(TEST_CONSUMER));
    }

    @Test  //Without content.
    public void setConsumerIdNoMembersTest() {
        template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
        assertTrue(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID).equals(TEST_SENDER));
    }

    private void addConsumerRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(
        new RouteBuilder() {
          @Override
          public void configure() {
            from("direct:start").routeId("start")
                .to("netty4-http:" + httpsRoute + "?sslContextParameters=#incomingSSLContextParameters&ssl=true&" +
                        "sslClientCertHeaders=true&needClientAuth=true&matchOnUriPrefix=true");
              //Address below from tak-vagval-test.xml
            from("netty4-http:https://localhost:19001/vardgivare-b/tjanst2?sslContextParameters=#outgoingSSLContextParameters&ssl=true")
                .to("mock:result");
          }
        });
    }
}
