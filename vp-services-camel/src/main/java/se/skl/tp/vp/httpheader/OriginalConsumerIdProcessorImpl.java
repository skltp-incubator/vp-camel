package se.skl.tp.vp.httpheader;

import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP013;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.ExceptionUtil;

@Service
@Slf4j
public class OriginalConsumerIdProcessorImpl implements OriginalConsumerIdProcessor {

  @Autowired
  ExceptionUtil exceptionUtil;

  public boolean isEnforceSenderIdCheck() {
    return enforceSenderIdCheck;
  }

  public void setEnforceSenderIdCheck(boolean enforceSenderIdCheck) {
    this.enforceSenderIdCheck = enforceSenderIdCheck;
  }

  @Autowired
  private CheckSenderAllowedToUseHeader checkSenderIdAgainstList;

  @Value("${" + PropertyConstants.APPROVE_THE_USE_OF_HEADER_ORIGINAL_CONSUMER + ":#{true}}")
  private boolean enforceSenderIdCheck;

  public void process(Exchange exchange) {
    String originalConsumer = null;

    if (exchange.getIn().getHeaders().containsKey(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)) {
      if (enforceSenderIdCheck) {
        String senderId = (String) exchange.getProperty(VPExchangeProperties.SENDER_ID);
        boolean ok = checkSenderIdAgainstList.isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader(senderId);
        if (!ok) {
          throw exceptionUtil.createVpSemanticException(VP013);
        }
      }
      originalConsumer = exchange.getIn().getHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class);
    }
    exchange.setProperty(VPExchangeProperties.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalConsumer);
  }

}
