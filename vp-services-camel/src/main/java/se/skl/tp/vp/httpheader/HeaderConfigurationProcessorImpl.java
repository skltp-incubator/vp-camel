package se.skl.tp.vp.httpheader;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import java.util.UUID;


@Service
public class HeaderConfigurationProcessorImpl implements HeaderConfigurationProcessor {

    @Autowired
    Environment env;
    private static boolean propagateCorrelationIdForHttps;
    private static Logger LOGGER = LogManager.getLogger(HttpSenderIdExtractorProcessorImpl.class);
    @Value("${" + PropertyConstants.PROPAGATE_CORRELATION_ID_FOR_HTTPS + "}")
    private String propagate;

    public String getPropagate() {
        return propagate;
    }

    public void setPropagate(String propagate) {
        this.propagate = propagate;
    }

    @Override
    public void process(Exchange exchange) {
        setOriginalConsumerId(exchange);
        propagateCorrelationIdToProducer(exchange);
    }

  private void propagateCorrelationIdToProducer(Exchange exchange) {
        propagateCorrelationIdForHttps = propagate.trim().equals("true");
        Message message = exchange.getIn();
        String correlationId = message.getHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, String.class);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            LOGGER.debug("Correlation id not found in http header. Created a new one:::" + correlationId);
        }
        //Maybe (not clear yet) needed for logging purposes, therefore set like in old VP
        exchange.setProperty(VPExchangeProperties.SKLTP_CORRELATION_ID, correlationId);
        String isHttps = ((String)exchange.getProperty(VPExchangeProperties.IS_HTTPS));
        if (isHttps == null) {
            exchange.getIn().setHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, correlationId);
        } else {
            if (propagateCorrelationIdForHttps) {
                exchange.getIn().setHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, correlationId);
            } else {
                exchange.getIn().setHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, null);
            }
        }
        exchange.removeProperty(VPExchangeProperties.IS_HTTPS);
    }

    private void setOriginalConsumerId(Exchange exchange) {
        Message message = exchange.getIn();
        //The original sender of the request, that might have been transferred by an RTjP. Can be null.
        String originalServiceconsumerHsaid = message.getHeader(VPExchangeProperties.ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class);
        String senderId = message.getHeader(HttpHeaders.X_VP_SENDER_ID, String.class);

        if (originalServiceconsumerHsaid != null && !originalServiceconsumerHsaid.trim().isEmpty()) {
            exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid);
            exchange.getIn().removeHeader(VPExchangeProperties.ORIGINAL_SERVICE_CONSUMER_HSA_ID);
        } else {
            if (senderId == null || senderId.isEmpty()) {
                senderId = (String) exchange.getProperties().get(VPExchangeProperties.SENDER_ID);
            }
            exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, senderId);
        }
    }
}
