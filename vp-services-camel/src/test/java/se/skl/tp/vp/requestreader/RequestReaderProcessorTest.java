package se.skl.tp.vp.requestreader;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = se.skl.tp.vp.BeansConfiguration.class)
public class RequestReaderProcessorTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    RequestReaderProcessor requestReaderProcessor;

    @Test
    public void testSendRivta20Message() throws Exception {
        String expectedBody = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:add=\"http://www.w3.org/2005/08/addressing\" xmlns:urn=\"urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1\">\n" +
                "   <soapenv:Header>\n" +
                "      <add:To>1</add:To>\n" +
                "   </soapenv:Header>\n" +
                "   <soapenv:Body>\n" +
                "      <urn:GetCertificateRequest>\n" +
                "         <urn:certificateId>?</urn:certificateId>\n" +
                "         <urn:nationalIdentityNumber>?</urn:nationalIdentityNumber>\n" +
                "         <!--You may enter ANY elements at this point-->\n" +
                "      </urn:GetCertificateRequest>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        resultEndpoint.expectedBodiesReceived(expectedBody);
        resultEndpoint.expectedPropertyReceived("LogicalAddress", "1");
        resultEndpoint.expectedPropertyReceived("tjanstekontrakt", "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1");

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
                        .process(requestReaderProcessor)
                        .to("mock:result");
            }
        };
    }
}