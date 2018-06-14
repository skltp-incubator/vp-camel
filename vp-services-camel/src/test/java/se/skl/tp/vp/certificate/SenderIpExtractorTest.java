package se.skl.tp.vp.certificate;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.vp.constants.VPExchangeProperties;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = se.skl.tp.vp.BeansConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class SenderIpExtractorTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    SenderIpExtractor senderIpExtractor;

    @Test
    public void extractIPFromHeader() throws Exception {
        String expectedBody = "test";

        resultEndpoint.expectedBodiesReceived(expectedBody);
        resultEndpoint.expectedPropertyReceived(VPExchangeProperties.SENDER_IP_ADRESS, "127.0.0.1");

        template.sendBody(expectedBody);
        resultEndpoint.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .to("netty4-http:http://localhost:12123/vp");

                from("netty4-http:http://localhost:12123/vp")
                        .process((Exchange exchange) -> {
                            String senderIpAdress = senderIpExtractor.extractSenderIpAdress(exchange.getIn());
                            exchange.setProperty(VPExchangeProperties.SENDER_IP_ADRESS, senderIpAdress);
                        })
                        .to("mock:result");
            }
        };
    }
}