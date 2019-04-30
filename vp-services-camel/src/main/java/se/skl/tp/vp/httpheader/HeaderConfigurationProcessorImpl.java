package se.skl.tp.vp.httpheader;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;


@Service
@Slf4j
public class HeaderConfigurationProcessorImpl implements HeaderConfigurationProcessor {

  @Value("${" + PropertyConstants.PROPAGATE_CORRELATION_ID_FOR_HTTPS + "}")
  private static boolean propagateCorrelationIdForHttps;


  @Override
  public void process(Exchange exchange) {
    setOriginalConsumerId(exchange);
    propagateCorrelationIdToProducer(exchange);
  }

  private void propagateCorrelationIdToProducer(Exchange exchange) {
    Message message = exchange.getIn();
    String correlationId = message.getHeader(HttpHeaders.X_SKLTP_CORRELATION_ID, String.class);
    if (correlationId == null || correlationId.trim().isEmpty()) {
      correlationId = UUID.randomUUID().toString();
      log.debug("Correlation id not found in http header. Created a new one:::" + correlationId);
    }
    //Maybe (not clear yet) needed for logging purposes, therefore set like in old VP
    exchange.setProperty(VPExchangeProperties.SKLTP_CORRELATION_ID, correlationId);
    Boolean isHttps = exchange.getProperty(VPExchangeProperties.IS_HTTPS, Boolean.class);
    if (isHttps==null || !isHttps) {
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
    //The original sender of the request, that might have been transferred by an RTjP. Can be null.
    String originalServiceconsumerHsaid = exchange.getIn().getHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class);
    exchange.setProperty(VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid);

    if (originalServiceconsumerHsaid == null  || originalServiceconsumerHsaid.trim().isEmpty()) {
       originalServiceconsumerHsaid = exchange.getProperty(VPExchangeProperties.SENDER_ID, String.class);
       exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid );
    }
    // This property is set on session for loggers
    exchange.setProperty(VPExchangeProperties.OUT_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid);
  }
}
