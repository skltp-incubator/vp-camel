package se.skl.tp.vp.getstatus;

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
import se.skl.tp.vp.status.GetStatusProcessor;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GetStatusTest extends CamelTestSupport {

    private static boolean isContextStarted = false;

    @Autowired
    private CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    GetStatusProcessor getStatusProcessor;

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
    public void getStatusResultTest() {
        resultEndpoint.setExchangePattern(ExchangePattern.InOut);
        String body = "aTestBody";
        template.sendBody("direct:start", body);
        assert(!resultEndpoint.getExchanges().isEmpty());
        assertNotNull(resultEndpoint.getExchanges().get(0).getIn().getBody());
        String s = (String) resultEndpoint.getExchanges().get(0).getIn().getBody();
        assert(s.startsWith("{") && s.endsWith("}") && s.contains("managementName"));
    }

    private void createRoute(CamelContext camelContext) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from("direct:start").routeDescription("GetStatus").id("Status")
                            .to("netty4-http:http://localhost:80/get")
                            .process(getStatusProcessor)
                            .to("mock:result");
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String createStatusReport() {
        return "A Json formatted string";
    }
}
