package se.skl.tp.vp.inneTest.requestreader;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.inneTest.TestBeanConfiguration;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.requestreader.RequestReaderProcessorXMLEventReader;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith( CamelSpringBootRunner.class )
@ContextConfiguration(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class RequestReaderProcessorTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    RequestReaderProcessor requestReaderProcessor;

    @Test
    public void testSendRivta20Message() throws Exception {
        resultEndpoint.expectedBodiesReceived(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);
        resultEndpoint.expectedPropertyReceived(VPExchangeProperties.RECEIVER_ID, "UnitTest");
        resultEndpoint.expectedPropertyReceived(VPExchangeProperties.RIV_VERSION, RequestReaderProcessorXMLEventReader.RIVTABP_20);
        resultEndpoint.expectedPropertyReceived(VPExchangeProperties.SERVICECONTRACT_NAMESPACE, "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1");

        template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);
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