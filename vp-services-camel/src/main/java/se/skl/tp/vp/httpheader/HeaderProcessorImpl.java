package se.skl.tp.vp.httpheader;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;


@Service
@Slf4j
public class HeaderProcessorImpl implements HeaderProcessor {

    @Autowired
    private IPWhitelistHandler ipWhitelistHandler;

    @Value("${" + PropertyConstants.PROPAGATE_CORRELATION_ID_FOR_HTTPS + "}")
    private boolean propagateCorrelationIdForHttps;

    @Value("${" + PropertyConstants.VP_HEADER_USER_AGENT + "}")
    private String vpHeaderUserAgent;

    @Value("${" + PropertyConstants.VP_HEADER_CONTENT_TYPE + "}")
    private String headerContentType;

    @Value("${" + PropertyConstants.VP_INSTANCE_ID + "}")
    private String vpInstanceId;

    public boolean getPropagateCorrelationIdForHttps() {
        return propagateCorrelationIdForHttps;
    }

    public void setPropagateCorrelationIdForHttps(boolean propagate) {
        this.propagateCorrelationIdForHttps = propagate;
    }


    @Override
    public void process(Exchange exchange) {
        setOriginalConsumerId(exchange);
        propagateCorrelationIdToProducer(exchange);
        propagateSenderIdAndVpInstanceIdToProducer(exchange);
        exchange.getIn().getHeaders().put(HttpHeaders.HEADER_USER_AGENT, vpHeaderUserAgent);
        exchange.getIn().getHeaders().put(HttpHeaders.HEADER_CONTENT_TYPE, headerContentType);
    }

    private void propagateSenderIdAndVpInstanceIdToProducer(Exchange exchange) {
        if (isHttpRequest(exchange)) {
            String senderId = (String) exchange.getProperties().get(VPExchangeProperties.SENDER_ID);
            exchange.getIn().getHeaders().put(HttpHeaders.X_VP_SENDER_ID, senderId);
            exchange.getIn().getHeaders().put(HttpHeaders.X_VP_INSTANCE_ID, vpInstanceId);
        }
    }

    private void propagateCorrelationIdToProducer(Exchange exchange) {
        String correlationId = exchange.getIn().getHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, String.class);

        if (StringUtils.isEmpty(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Correlation id not found in http header. Created a new one:::" + correlationId);
        }
        exchange.setProperty(VPExchangeProperties.SKLTP_CORRELATION_ID, correlationId);

        if (isHttpRequest(exchange)) {
            exchange.getIn().setHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, correlationId);
        } else {
            if (propagateCorrelationIdForHttps) {
                exchange.getIn().setHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, correlationId);
            } else {
                exchange.getIn().setHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, null);
            }
        }
    }

    private void setOriginalConsumerId(Exchange exchange) {
        String originalServiceConsumerHsaId = exchange.getIn().getHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class);
        exchange.setProperty(VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceConsumerHsaId);

        if (StringUtils.isEmpty(originalServiceConsumerHsaId)) {
            String senderId = exchange.getProperty(VPExchangeProperties.SENDER_ID, String.class);
            exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, senderId);
            exchange.setProperty(VPExchangeProperties.OUT_ORIGINAL_SERVICE_CONSUMER_HSA_ID, senderId);
        } else {
            exchange.setProperty(VPExchangeProperties.OUT_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceConsumerHsaId);
        }
    }


    private boolean isHttpRequest(Exchange exchange) {
        return exchange.getProperty(VPExchangeProperties.VAGVAL, String.class).contains("http://");
    }
}


