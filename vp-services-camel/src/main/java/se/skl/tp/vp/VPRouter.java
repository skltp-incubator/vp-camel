package se.skl.tp.vp;

import static org.apache.camel.builder.PredicateBuilder.or;

import io.netty.handler.timeout.ReadTimeoutException;
import java.net.SocketException;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.skl.tp.vp.certificate.CertificateExtractorProcessor;
import se.skl.tp.vp.config.HttpHeaderFilterRegexp;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.ExceptionMessageProcessor;
import se.skl.tp.vp.errorhandling.HandleEmptyResponseProcessor;
import se.skl.tp.vp.errorhandling.HandleProducerExceptionProcessor;
import se.skl.tp.vp.httpheader.HeaderProcessor;
import se.skl.tp.vp.httpheader.HttpSenderIdExtractorProcessor;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.timeout.RequestTimoutProcessor;
import se.skl.tp.vp.vagval.BehorighetProcessor;
import se.skl.tp.vp.vagval.RivTaProfilProcessor;
import se.skl.tp.vp.vagval.VPEventNotifierSupport;
import se.skl.tp.vp.vagval.VagvalProcessor;
import se.skl.tp.vp.wsdl.WsdlProcessor;

@Component
public class VPRouter extends RouteBuilder {

    public static final String VP_HTTP_ROUTE = "vp-http-route";
    public static final String VP_HTTPS_ROUTE = "vp-https-route";
    public static final String VAGVAL_ROUTE = "vagval-route";
    public static final String TO_PRODUCER_ROUTE = "to-producer-route";
    public static final String NETTY4_HTTP_FROM = "netty4-http:{{vp.http.route.url}}?matchOnUriPrefix=true";
    public static final String NETTY4_HTTP_TOD = "netty4-http:${property.vagval}?useRelativePath=true";
    public static final String DIRECT_VP = "direct:vp";
    public static final String DIRECT_PRODUCER_ROUTE = "direct:to-producer";
    public static final String NETTY4_HTTPS_INCOMING_FROM = "netty4-http:{{vp.https.route.url}}?sslContextParameters=#incomingSSLContextParameters&ssl=true&sslClientCertHeaders=true&needClientAuth=true&matchOnUriPrefix=true";
    public static final String NETTY4_HTTPS_OUTGOING_TOD = "netty4-http:${property.vagval}?sslContextParameters=#outgoingSSLContextParameters&ssl=true&useRelativePath=true";

    public static final String VAGVAL_PROCESSOR_ID = "VagvalProcessor";
    public static final String BEHORIGHET_PROCESSOR_ID = "BehorighetProcessor";
    public static final String LOG_ERROR_METHOD = "logError(*)";
    public static final String LOG_RESP_OUT_METHOD = "logRespOut(*)";
    public static final String LOG_REQ_IN_METHOD = "logReqIn(*)";
    public static final String LOG_REQ_OUT_METHOD = "logReqOut(*)";
    public static final String LOG_RESP_IN_METHOD = "logRespIn(*)";


    @Autowired
    HeaderProcessor headerProcessor;

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
            .bean(MessageInfoLogger.class, LOG_ERROR_METHOD)
            .bean(MessageInfoLogger.class, LOG_RESP_OUT_METHOD)
            .handled(true);


        from(NETTY4_HTTPS_INCOMING_FROM).routeId(VP_HTTPS_ROUTE)
            .choice().when(header("wsdl").isNotNull())
                .process(wsdlProcessor)
            .otherwise()
                .process(certificateExtractorProcessor)
                .to(DIRECT_VP)
                .bean(MessageInfoLogger.class, LOG_RESP_OUT_METHOD)
            .end();

        from(NETTY4_HTTP_FROM).routeId(VP_HTTP_ROUTE)
            .choice().when(header("wsdl").isNotNull())
                .process(wsdlProcessor)
            .otherwise()
                .process(httpSenderIdExtractorProcessor)
                .to(DIRECT_VP)
                .bean(MessageInfoLogger.class, LOG_RESP_OUT_METHOD)
            .end();

        from(DIRECT_VP).routeId(VAGVAL_ROUTE)
            .streamCaching()
            .setProperty(VPExchangeProperties.HTTP_URL_IN,  header("CamelHttpUrl"))
            .setProperty(VPExchangeProperties.VP_X_FORWARDED_HOST,  header("{{http.forwarded.header.host}}"))
            .setProperty(VPExchangeProperties.VP_X_FORWARDED_PORT,  header("{{http.forwarded.header.port}}"))
            .setProperty(VPExchangeProperties.VP_X_FORWARDED_PROTO,  header("{{http.forwarded.header.proto}}"))
            .process(requestReaderProcessor)
            .bean(MessageInfoLogger.class, LOG_REQ_IN_METHOD)
            .process(vagvalProcessor).id(VAGVAL_PROCESSOR_ID)
            .process(behorighetProcessor).id(BEHORIGHET_PROCESSOR_ID)
            .process(headerProcessor)
            .process(requestTimoutProcessor)
            .process(rivTaProfilProcessor)
            .to(DIRECT_PRODUCER_ROUTE)
            .choice().when(or(body().isNull(), body().isEqualTo("")))
                .log(LoggingLevel.ERROR, "Response from producer is empty")
                .process(handleEmptyResponseProcessor)
                .bean(MessageInfoLogger.class, LOG_ERROR_METHOD)
            .end();

        from(DIRECT_PRODUCER_ROUTE)
            .routeId(TO_PRODUCER_ROUTE)
            .onException(SocketException.class)
                .log(LoggingLevel.ERROR, "OnBefore Redelivery Global")
                .redeliveryDelay("{{vp.producer.retry.delay}}")
                .maximumRedeliveries("{{vp.producer.retry.attempts}}")
                    .logRetryAttempted(true)
                    .retryAttemptedLogLevel(LoggingLevel.ERROR)
                    .logRetryStackTrace(false)
                .log(LoggingLevel.ERROR, "Catched in test route: ${exception}")
            .process(handleProducerExceptionProcessor)
                .bean(MessageInfoLogger.class, LOG_ERROR_METHOD)
                .bean(MessageInfoLogger.class, LOG_RESP_OUT_METHOD)
                .handled(true)
            .end()
            .onException(ReadTimeoutException.class)
                .log(LoggingLevel.ERROR, "Catched exception when calling producer: ${exception}")
                .process(handleProducerExceptionProcessor)
                .bean(MessageInfoLogger.class, LOG_ERROR_METHOD)
                .bean(MessageInfoLogger.class, LOG_RESP_OUT_METHOD)
                .handled(true)
            .end()
            .removeHeaders(reg.getRemoveRegExp(),reg.getKeepRegExp())
            .bean(MessageInfoLogger.class, LOG_REQ_OUT_METHOD)
            .choice().when(exchangeProperty(VPExchangeProperties.VAGVAL).contains("https://"))
                    .recipientList(simple(NETTY4_HTTPS_OUTGOING_TOD))
                    .endChoice()
                .otherwise()
                    .recipientList(simple(NETTY4_HTTP_TOD))
                    .endChoice()
            .end()
            .setHeader(HttpHeaders.X_SKLTP_PRODUCER_RESPONSETIME, exchangeProperty(VPEventNotifierSupport.LAST_ENDPOINT_RESPONSE_TIME))
            .bean(MessageInfoLogger.class, LOG_RESP_IN_METHOD)
            .end();




    }
}
