package se.skl.tp.vp.httpheader;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.errorhandling.ExceptionUtil;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;

@Service
@Log4j2
public class FeedbackProtectionProcessorImpl implements FeedbackProtectionProcesssor{

  private ExceptionUtil exceptionUtil;
  private SenderIpExtractor senderIpExtractor;
  private String platformId;

  public FeedbackProtectionProcessorImpl(ExceptionUtil exceptionUtil, SenderIpExtractor senderIpExtractor,
    @Value("${" + HttpHeaders.X_VP_PLATFORM_ID + ":#{null}}") String platformId) {
    this.exceptionUtil = exceptionUtil;
    this.senderIpExtractor = senderIpExtractor;
    if (platformId == null) {
      log.warn("Platform id NOT configured! No feedback control possible.");
    }
    this.platformId = platformId;
  }

  public void setPlatformId(String s) {
    platformId = s;
  }

  public String getPlatformId() {
    return platformId;
  }

  public void process(Exchange exchange) {
    Message message = exchange.getIn();
    String callerRemoteAddress = senderIpExtractor.getCallerRemoteAddress(message);
    String forwardedForIpAdress = senderIpExtractor.getForwardedForAddress(message);
    String senderIpAdress = forwardedForIpAdress != null ? forwardedForIpAdress : callerRemoteAddress;
    String incomingPlatformId = (String) exchange.getIn().getHeader(HttpHeaders.X_VP_PLATFORM_ID);
    if (!StringUtils.isEmpty(platformId) && !StringUtils.isEmpty(incomingPlatformId) && incomingPlatformId.equals(platformId)) {
      log.error("Message contained header {}. Faulty routing, possible loop. Throwing VPexception VP014.", HttpHeaders.X_VP_PLATFORM_ID);
      throw exceptionUtil.createVpSemanticException(VpSemanticErrorCodeEnum.VP014, " Sender IP-address: " + senderIpAdress);
    } else {
      if (!StringUtils.isEmpty(platformId)) {
        message.setHeader(HttpHeaders.X_VP_PLATFORM_ID, platformId);
      }
    }
  }
}
