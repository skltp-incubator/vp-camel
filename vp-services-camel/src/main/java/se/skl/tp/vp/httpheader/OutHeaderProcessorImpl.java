package se.skl.tp.vp.httpheader;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticException;

@Service
@Slf4j
public class OutHeaderProcessorImpl implements OutHeaderProcessor {

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
  public void process(Exchange exchange) throws Exception {
    setOriginalConsumerId(exchange);
    propagateCorrelationIdToProducer(exchange);
    propagateSenderIdAndVpInstanceIdToProducer(exchange);
    exchange.getIn().getHeaders().put(HttpHeaders.HEADER_USER_AGENT, vpHeaderUserAgent);
    exchange.getIn().getHeaders().put(HttpHeaders.HEADER_CONTENT_TYPE, headerContentType);
  }

  /*
   * If http, propagate x-vp-sender-id and x-vp-instance-id from this VP instance as an outbound http header as they are
   * both needed together for another VP to determine if x-vp-sender-id is valid.
   */
  private void propagateSenderIdAndVpInstanceIdToProducer(Exchange exchange) {
    if (isHttpRequest(exchange)) {
      String senderId = (String) exchange.getProperties().get(VPExchangeProperties.SENDER_ID);
      exchange.getIn().getHeaders().put(HttpHeaders.X_VP_SENDER_ID, senderId);
      exchange.getIn().getHeaders().put(HttpHeaders.X_VP_INSTANCE_ID, vpInstanceId);
    }
  }

  /**
   * If correlationId wasn't set in incoming request, it has been creeated and set in CorrelationIdProcessorImpl..
   * @param exchange
   */
  private void propagateCorrelationIdToProducer(Exchange exchange) {
    String correlationId = (String) exchange.getProperty(VPExchangeProperties.SKLTP_CORRELATION_ID);
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

  /**
   * If exchange.getProperty(IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID) NOT is null/empty, then
   * HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID has already been set on this exchange,
   * checked in OriginalConsumerIdProcessorImpl.
   * @param exchange
   */
  private void setOriginalConsumerId(Exchange exchange) {
    String originalServiceConsumerHsaId =
        (String) exchange.getProperty(VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID);

    if (StringUtils.isEmpty(originalServiceConsumerHsaId)) {
      originalServiceConsumerHsaId = exchange.getProperty(VPExchangeProperties.SENDER_ID, String.class);
      exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceConsumerHsaId);
    }
    exchange.setProperty(VPExchangeProperties.OUT_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceConsumerHsaId);
  }

  private boolean isHttpRequest(Exchange exchange) {
    return exchange.getProperty(VPExchangeProperties.VAGVAL, String.class).contains("http://");
  }
}
