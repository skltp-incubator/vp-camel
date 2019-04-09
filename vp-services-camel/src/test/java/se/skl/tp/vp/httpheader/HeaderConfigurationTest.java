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
import se.skl.tp.vp.integrationtests.utils.MockProducer;

import java.util.HashMap;
import java.util.Map;

import static se.skl.tp.vp.errorhandling.ErrorInResponseTest.MOCK_PRODUCER_ADDRESS;
import static se.skl.tp.vp.errorhandling.ErrorInResponseTest.VP_ADDRESS;


@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HeaderConfigurationTest extends CamelTestSupport {


    private static MockProducer mockProducer;

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
            mockProducer = new MockProducer(camelContext, MOCK_PRODUCER_ADDRESS);
            addConsumerRoute(camelContext);
            camelContext.start();
            isContextStarted=true;
        }
        resultEndpoint.reset();

    }

    @Test
    public void positiveHeaderConfigTest() throws Exception {
        String body = "aTestBody";
        Map headers = createHeaders();
        headers.put(VPExchangeProperties.SKLTP_CORRELATION_ID, "aTestCorrelationId");
        headers.put(VPExchangeProperties.ORIGINAL_SERVICE_CONSUMER_HSA_ID, "aTestConsumerId");
        template.sendBodyAndHeaders(body, headers);
        assert("aTestConsumerId".equals(resultEndpoint.getReceivedExchanges().get(0).getProperties().get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)));
        assert("aTestCorrelationId".equals(resultEndpoint.getReceivedExchanges().get(0).getProperties().get(HttpHeaders.X_SKLTP_CORRELATION_ID)));
    }

    @Test
    public void negativeHeaderConfigurationTest() throws Exception {
        String body = "aTestBody";
        Map headers = createHeaders();
        template.sendBodyAndHeaders(body, headers);
        assert("UnitTest".equals(resultEndpoint.getReceivedExchanges().get(0).getProperties().get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)));
        String correlationId = (String) resultEndpoint.getReceivedExchanges().get(0).getProperties().get(HttpHeaders.X_SKLTP_CORRELATION_ID);
        assertNotNull(correlationId);
        assert(correlationId.length() > 35);
        assertNotEquals("aTestCorrelationId", correlationId);
    }

    private void addConsumerRoute(CamelContext camelContext) throws Exception {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("direct:start").routeDescription("Consumer").id("Consumer")
                            .process(headerConfigurationProcessor)
                            .to("netty4-http:"+VP_ADDRESS)
                            .to("mock:result"); ;
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Map createHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.X_VP_SENDER_ID, "UnitTest");
        headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
        headers.put("X-Forwarded-For", "1.2.3.4");
        return headers;
    }
}
