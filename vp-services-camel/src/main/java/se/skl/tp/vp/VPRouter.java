package se.skl.tp.vp;

import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.netty4.http.NettyHttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skl.tp.vp.certificate.CertificateExtractorProcessor;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.HandleEmptyResponseProcessor;
import se.skl.tp.vp.errorhandling.ExceptionMessageProcessor;
import se.skl.tp.vp.errorhandling.HandleProducerExceptionProcessor;
import se.skl.tp.vp.httpheader.HeaderConfigurationProcessor;
import se.skl.tp.vp.config.HttpHeaderFilterRegexp;
import se.skl.tp.vp.httpheader.HttpSenderIdExtractorProcessor;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.timeout.RequestTimoutProcessor;
import se.skl.tp.vp.vagval.*;
import se.skl.tp.vp.wsdl.WsdlProcessor;

import java.net.SocketException;

import static org.apache.camel.builder.PredicateBuilder.or;

@Component
public class VPRouter extends RouteBuilder {

    public static final String VP_HTTP_ROUTE = "vp-http-route";
    public static final String VP_HTTPS_ROUTE = "vp-https-route";
    public static final String VAGVAL_ROUTE = "vagval-route";

    public static final String NETTY4_HTTP_FROM = "netty4-http:{{vp.http.route.url}}?matchOnUriPrefix=true";
    public static final String NETTY4_HTTP_TOD = "netty4-http:${property.vagval}";
    public static final String DIRECT_VP = "direct:vp";
    public static final String NETTY4_HTTPS_INCOMING_FROM = "netty4-http:{{vp.https.route.url}}?sslContextParameters=#incomingSSLContextParameters&ssl=true&sslClientCertHeaders=true&needClientAuth=true&matchOnUriPrefix=true";
    public static final String NETTY4_HTTPS_OUTGOING_TOD = "netty4-http:${property.vagval}?sslContextParameters=#outgoingSSLContextParameters&ssl=true";
    public static final String VAGVAL_PROCESSOR_ID = "VagvalProcessor";
    public static final String BEHORIGHET_PROCESSOR_ID = "BehorighetProcessor";

    @Autowired
    HeaderConfigurationProcessor headerConfigurationProcessor;

    @Autowired
    VagvalProcessor vagvalProcessor;

    @Autowired
    BehorighetProcessor behorighetProcessor;

    @Autowired
    CertificateExtractorProcessor certificateExtractorProcessor;

    @Autowired
    HttpSenderIdExtractorProcessor httpSenderIdExtractorProcessor;

    @Autowired
    RequestReaderProcessor requestReaderProcessor;

    @Autowired
    ExceptionMessageProcessor exceptionMessageProcessor;

    @Autowired
    HandleEmptyResponseProcessor handleEmptyResponseProcessor;

    @Autowired
    RivTaProfilProcessor rivTaProfilProcessor;

    @Autowired
    WsdlProcessor wsdlProcessor;

    @Autowired
    RequestTimoutProcessor requestTimoutProcessor;

    @Autowired
    HandleProducerExceptionProcessor handleProducerExceptionProcessor;

    @Autowired
    private HttpHeaderFilterRegexp reg;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Catched exception: ${exception}")
                .process(exceptionMessageProcessor)
                .bean(MessageInfoLogger.class, "logError(*)")
                .bean(MessageInfoLogger.class, "logRespOut(*)")
                .handled(true);

        from(NETTY4_HTTPS_INCOMING_FROM).routeId(VP_HTTPS_ROUTE)
                .choice().when(header("wsdl").isNotNull())
                    .process(wsdlProcessor)
                .otherwise()
                    .setProperty(VPExchangeProperties.IS_HTTPS, constant(true))
                    .process(certificateExtractorProcessor)
                    .to(DIRECT_VP)
                    .bean(MessageInfoLogger.class, "logRespOut(*)")
                .end();

        from(NETTY4_HTTP_FROM).routeId(VP_HTTP_ROUTE)
                .choice().when(header("wsdl").isNotNull())
                    .process(wsdlProcessor)
                .otherwise()
                    .setProperty(VPExchangeProperties.IS_HTTPS, constant(false))
                    .process(httpSenderIdExtractorProcessor)
                    .to(DIRECT_VP)
                    .bean(MessageInfoLogger.class, "logRespOut(*)")
                .end();

        from(DIRECT_VP).routeId(VAGVAL_ROUTE)
                .streamCaching()
                .setProperty(VPExchangeProperties.HTTP_URL_IN,  simple("header.CamelHttpUrl"))
                .setProperty(VPExchangeProperties.VP_X_FORWARDED_HOST,  simple("header.{{http.forwarded.header.host}}"))
                .setProperty(VPExchangeProperties.VP_X_FORWARDED_PORT,  simple("header.{{http.forwarded.header.port}}"))
                .setProperty(VPExchangeProperties.VP_X_FORWARDED_PROTO,  simple("header.{{http.forwarded.header.proto}}"))
                .process(requestReaderProcessor)
                .process(headerConfigurationProcessor)
                .bean(MessageInfoLogger.class, "logReqIn(*)")
                .process(vagvalProcessor).id(VAGVAL_PROCESSOR_ID)
                .process(behorighetProcessor).id(BEHORIGHET_PROCESSOR_ID)
                .process(requestTimoutProcessor)
                .process(rivTaProfilProcessor)
                .doTry()
                    .choice()
                        .when(exchangeProperty(VPExchangeProperties.VAGVAL).contains("https://"))
                            .removeHeaders(reg.getRemoveRegExp(),reg.getKeepRegExp())
                            .bean(MessageInfoLogger.class, "logReqOut(*)")
                            .recipientList(simple(NETTY4_HTTPS_OUTGOING_TOD))
                            .setHeader(HttpHeaders.X_SKLTP_PRODUCER_RESPONSETIME, exchangeProperty(VPEventNotifierSupport.LAST_ENDPOINT_RESPONSE_TIME))
                            .bean(MessageInfoLogger.class, "logRespIn(*)").endChoice()
                        .otherwise()
                            .removeHeaders(reg.getRemoveRegExp(),reg.getKeepRegExp())
                            .bean(MessageInfoLogger.class, "logReqOut(*)")
                            .recipientList(simple(NETTY4_HTTP_TOD))
                            .setHeader(HttpHeaders.X_SKLTP_PRODUCER_RESPONSETIME, exchangeProperty(VPEventNotifierSupport.LAST_ENDPOINT_RESPONSE_TIME))
                            .bean(MessageInfoLogger.class, "logRespIn(*)")
                    .endChoice()
                .endDoTry()
                .doCatch(SocketException.class, ReadTimeoutException.class, NettyHttpOperationFailedException.class)
                    .log(LoggingLevel.ERROR, "Catched exception when calling producer: ${exception}")
                    .process(handleProducerExceptionProcessor)
                    .bean(MessageInfoLogger.class, "logError(*)")
                .end()
                .choice().when(or(body().isNull(), body().isEqualTo("")))
                    .log(LoggingLevel.ERROR, "Response from producer is empty")
                    .process(handleEmptyResponseProcessor)
                    .bean(MessageInfoLogger.class, "logError(*)")
                .endChoice()
                .end();
    }
}
