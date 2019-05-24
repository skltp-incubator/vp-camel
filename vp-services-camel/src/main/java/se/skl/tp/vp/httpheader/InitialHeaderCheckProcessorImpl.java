package se.skl.tp.vp.httpheader;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

import java.util.UUID;

@Service
@Slf4j
public class InitialHeaderCheckProcessorImpl implements InitialHeaderCheckProcessor {

  @Autowired private IPConsumerListHandler ipConsumerListHandler;

  @Value("${" + PropertyConstants.ENFORCE_CONSUMER_LIST + ":#{true}}")
  private boolean enforceConsumerList;

  public void process(Exchange exchange) throws Exception {
    String originalConsumer = null;
    String correlationId = null;

    if (exchange
        .getIn()
        .getHeaders()
        .containsKey(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)) {
      if (enforceConsumerList) {
        String senderIP = (String) exchange.getProperty(VPExchangeProperties.SENDER_ID);
        boolean ok = checkIfApproved(senderIP);
        if (!ok) {
          throw new VpSemanticException(
              VpSemanticErrorCodeEnum.VP013 + " Sender NOT on ConsumerList:" + senderIP,
              VpSemanticErrorCodeEnum.VP013);
        }
      }
      originalConsumer =
          ""
              + exchange
                  .getIn()
                  .getHeaders()
                  .get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
    }
    exchange.setProperty(
        VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalConsumer);

    if (exchange.getIn().getHeaders().containsKey(HttpHeaders.X_SKLTP_CORRELATION_ID)) {
      correlationId = "" + exchange.getIn().getHeaders().get(HttpHeaders.X_SKLTP_CORRELATION_ID);
    }
    if (StringUtils.isEmpty(correlationId)) {
      correlationId = UUID.randomUUID().toString();
    }
    exchange.setProperty(VPExchangeProperties.SKLTP_CORRELATION_ID, correlationId);

    // TODO More headers to check?
  }

  private boolean checkIfApproved(String senderIP) {
    return ipConsumerListHandler.isCallerOnConsumerList(senderIP);
  }
}
