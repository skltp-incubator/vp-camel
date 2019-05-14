package se.skl.tp.vp.httpheader;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
//import se.skl.tp.vp.exceptions.VpSemanticException;


@Service
@Slf4j
public class HeaderConfigurationProcessorImpl implements HeaderConfigurationProcessor {

  @Value("${" + PropertyConstants.PROPAGATE_CORRELATION_ID_FOR_HTTPS + "}")
  private boolean propagateCorrelationIdForHttps;

  @Value("${" + PropertyConstants.VP_HEADER_USER_AGENT + "}")
  private String vpHeaderUserAgent;

  @Value("${" + PropertyConstants.VP_HEADER_CONTENT_TYPE + "}")
  private String headerContentType;

  @Value("${" + PropertyConstants.VP_INSTANCE_ID + "}")
  private String vpInstanceId;

  @Autowired
  private IPWhitelistHandler ipWhitelistHandler;

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

  /*
   * Propagate x-vp-sender-id and x-vp-instance-id from this VP instance as an outbound http property as they are both needed
   * together for another VP to determine if x-vp-sender-id is valid to use.
   */
  private void propagateSenderIdAndVpInstanceIdToProducer(Exchange exchange) {
    Boolean isHttps = exchange.getProperty(VPExchangeProperties.IS_HTTPS, Boolean.class);
    if (!isHttps) {
      String senderId = (String) exchange.getProperties().get(VPExchangeProperties.SENDER_ID);
      if (senderId != null) {
        exchange.getIn().getHeaders().put(HttpHeaders.X_VP_SENDER_ID, senderId);
      }
      exchange.getIn().getHeaders().put(HttpHeaders.X_VP_INSTANCE_ID, vpInstanceId);
    }
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

  private void setOriginalConsumerId(Exchange exchange) { //throws VpSemanticException
    boolean exist = exchange.getIn().getHeaders().containsKey(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
    //The original sender of the request, that might have been transferred by an RTjP. Can be null.
    String originalServiceconsumerHsaid = exchange.getIn().getHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class);
    exchange.setProperty(VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid);
    //If the header is set, check if approved and log (Jira NTP-832)
    if (exist) {
      //Log and Check if approved..
      boolean ok = checkIfSenderIsApproved(exchange);
      //log.info("Sender");

      if (ok) {
        //if null or empty, set senderId
        if (originalServiceconsumerHsaid == null  || originalServiceconsumerHsaid.trim().isEmpty()) {
          originalServiceconsumerHsaid = setSenderIdAsOriginalConsumer(exchange);
        }
      } else {
        //throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002 + " Sender NOT on ConsumerList: ",
          //      VpSemanticErrorCodeEnum.VP002);
        System.out.println("ERROR should be thrown, because list existed and sender was NOT on it..");
      }
    } else {
      //if nonexisting, set senderId
      originalServiceconsumerHsaid = setSenderIdAsOriginalConsumer(exchange);
    }
    // This property is set on session for loggers
    exchange.setProperty(VPExchangeProperties.OUT_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid);
  }

  public String setSenderIdAsOriginalConsumer(Exchange exchange) {
    String s = exchange.getProperty(VPExchangeProperties.SENDER_ID, String.class);
    exchange.getIn().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, s );
    return s;
  }

  private boolean checkIfSenderIsApproved(Exchange exchange) {
    String sender = exchange.getProperty(VPExchangeProperties.SENDER_ID, String.class);
    return ipWhitelistHandler.isCallerOnConsumerList(sender);
  }
}


