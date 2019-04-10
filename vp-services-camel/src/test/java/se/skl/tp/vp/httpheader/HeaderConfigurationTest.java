package se.skl.tp.vp.httpheader;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;

import java.util.HashMap;
import java.util.Map;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HeaderConfigurationTest extends CamelTestSupport {

    private static boolean isContextStarted = false;

    @Autowired
    private CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    HeaderConfigurationProcessorImpl headerConfigurationProcessor;

    @Before
    public void setUp() throws Exception {
        if(!isContextStarted){
            createRoute(camelContext);
            camelContext.start();
            isContextStarted=true;
        }
        resultEndpoint.reset();
    }

    @Test
    public void positiveCorrelationIdTest() {
        String body = "aTestBody";
        Map headers = createHeaders();
        headers.put(VPExchangeProperties.SKLTP_CORRELATION_ID, "aTestCorrelationId");
        template.sendBodyAndHeaders(body, headers);
        assert("aTestCorrelationId".equals(resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_SKLTP_CORRELATION_ID)));
    }

    @Test
    public void positiveOriginalConsumerTest() {
        String body = "aTestBody";
        Map headers = createHeaders();
        headers.put(VPExchangeProperties.ORIGINAL_SERVICE_CONSUMER_HSA_ID, "aTestConsumerId");
        template.sendBodyAndHeaders(body, headers);
        assert("aTestConsumerId".equals(resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)));
    }

    @Test
    public void negativeCorrelationIdTest() {
        String body = "aTestBody";
        Map headers = createHeaders();
        template.sendBodyAndHeaders(body, headers);
        String correlationId = (String) resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_SKLTP_CORRELATION_ID);
        //If no SKLTP_CORRELATION_ID is present in the request, it should be generated.
        assertNotNull(correlationId);
        assertNotEquals("aTestCorrelationId", correlationId);
        assert(correlationId.length() > 35);
    }

    @Test
    public void negativeOriginalConsumerTest() {
        String body = "aTestBody";
        Map headers = createHeaders();
        template.sendBodyAndHeaders(body, headers);
        //The X_VP_SENDER_ID should be used as X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, if ORIGINAL_SERVICE_CONSUMER_HSA_ID isn't present in request.
        assert("testSenderId".equals(resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)));
    }

    private void createRoute(CamelContext camelContext) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct:start").routeDescription("Consumer").id("Consumer")
                            .process(headerConfigurationProcessor)
                            .to("mock:result");
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Map createHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.X_VP_SENDER_ID, "testSenderId");
        headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
        headers.put("X-Forwarded-For", "2.3.4.5");
        headers.put(VPExchangeProperties.RECEIVER_ID, "test1956receiver");
        return headers;
    }
}
