package se.skl.tp.vp.httpheader;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;

import java.util.UUID;

@Service
public class HeaderConfigurationProcessorImpl implements HeaderConfigurationProcessor{

    private static Logger LOGGER = LogManager.getLogger(HttpSenderIdExtractorProcessorImpl.class);
    private boolean propagateCorrelationIdForHttps;

    @Override
    public void process(Exchange exchange) throws Exception {
        setOriginalConsumerId(exchange);
        propagateCorrelationIdToProducer(exchange, propagateCorrelationIdForHttps);
    }

    public void setPropagateCorrelationIdForHttps(final Boolean propagateCorrelationIdForHttps) {
        this.propagateCorrelationIdForHttps = propagateCorrelationIdForHttps;
    }

    private void propagateCorrelationIdToProducer(Exchange exchange, Boolean propagateCorrelationIdForHttps) {
        Message message = exchange.getIn();
        String correlationId = message.getHeader(VPExchangeProperties.SKLTP_CORRELATION_ID, String.class);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            LOGGER.debug("Correlation id not found in http header. Will create a new one!");
            correlationId = UUID.randomUUID().toString();
        }
        String isHttps = ((String)exchange.getProperty(VPExchangeProperties.IS_HTTPS));
        if (isHttps == null) {
            exchange.getIn().setHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, correlationId);
        } else {
            if (propagateCorrelationIdForHttps) {
                exchange.getIn().setHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, correlationId);
            }
            exchange.removeProperty(VPExchangeProperties.IS_HTTPS);
        }
    }

    private void setOriginalConsumerId(Exchange exchange) {
        Message message = exchange.getIn();
        //The original sender of the request, that might have been transferred by an RTjP. Can be null.
        String originalServiceconsumerHsaid = message.getHeader(VPExchangeProperties.ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class);
        String senderId = message.getHeader(HttpHeaders.X_VP_SENDER_ID, String.class);

        if (originalServiceconsumerHsaid != null && !originalServiceconsumerHsaid.trim().isEmpty()) {
            exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid);
        } else {
            exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, senderId);
        }
    }
}
