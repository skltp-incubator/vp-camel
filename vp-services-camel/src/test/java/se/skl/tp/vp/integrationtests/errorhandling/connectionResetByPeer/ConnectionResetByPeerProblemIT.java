package se.skl.tp.vp.integrationtests.errorhandling.connectionResetByPeer;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.util.CharsetUtil;
import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.integrationtests.httpheader.HeadersUtil;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.util.LeakDetectionBaseTest;
import se.skl.tp.vp.util.TestLogAppender;

import java.util.Map;

import static org.apache.camel.test.junit4.TestSupport.assertStringContains;
import static org.junit.Assert.assertEquals;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.createGetCertificateRequest;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:application.properties", "classpath:vp-messages.properties"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@StartTakService
public class ConnectionResetByPeerProblemIT extends LeakDetectionBaseTest {

    @Autowired
    TestConsumer testConsumer;

    @Autowired
    CamelContext camelContext;

    TestLogAppender testLogAppender = TestLogAppender.getInstance();

    @Test
    public void errorWithClosedChanel() throws InterruptedException {
        ServerBehavior b = (ChannelHandlerContext ctx) -> {
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, OK,
                    Unpooled.copiedBuffer("<mocked answer/>", CharsetUtil.UTF_8));

            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        };

        ResetByPeerServer.startServer(19007, b);

        Map<String, Object> headers = HeadersUtil.createHttpHeaders();
        String receiver = "HttpProducerResetByPeer";
        String request = createGetCertificateRequest(receiver);
        testConsumer.sendHttpRequestToVP(request, headers);

        Thread.sleep(6000);
        ResetByPeerServer.stopServer();

        assertEquals(0, testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    }

    @Test
    public void errorWithLiveChanel() throws InterruptedException {
        ServerBehavior b = (ChannelHandlerContext ctx) -> {
            ctx.channel().close();
        };

        ResetByPeerServer.startServer(19007, b);

        Map<String, Object> headers = HeadersUtil.createHttpHeaders();
        String receiver = "HttpProducerResetByPeer";
        String request = createGetCertificateRequest(receiver);
        testConsumer.sendHttpRequestToVP(request, headers);

        assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
        assertStringContains(testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0), "An existing connection was forcibly closed by");

        ResetByPeerServer.stopServer();
    }
}
