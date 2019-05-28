package se.skl.tp.vp.httpheader;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

@Service
@Slf4j
public class OriginalConsumerIdProcessorImpl implements OriginalConsumerIdProcessor {

  public static String MESSAGE = " Property approve.to.use.header.original.consumerId was configured true and senderId was NOT on property list sender.id.allowed.list. Sender was:";

  @Autowired private CheckSenderAllowedToUseHeader checkSenderIdAgainstList;

  @Value("${" + PropertyConstants.APPROVE_THE_USE_OF_HEADER_ORIGINAL_CONSUMER + ":#{true}}")
  private boolean enforceSenderIdCheck;

  public void process(Exchange exchange) throws Exception {
    String originalConsumer = null;

    if (exchange.getIn().getHeaders().containsKey(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)) {
      if (enforceSenderIdCheck) {
        String senderId = (String) exchange.getProperty(VPExchangeProperties.SENDER_ID);
        boolean ok = checkSenderIdAgainstList.isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader(senderId);
        if (!ok) {
          throw new VpSemanticException(
              VpSemanticErrorCodeEnum.VP013 + MESSAGE + senderId,
              VpSemanticErrorCodeEnum.VP013);
        }
      }
      originalConsumer = "" + exchange.getIn().getHeaders().get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
    }
    exchange.setProperty(
        VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalConsumer);
  }
}
