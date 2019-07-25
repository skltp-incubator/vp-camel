package se.skl.tp.vp.httpheader;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import se.skl.tp.vp.certificate.HeaderCertificateHelper;
import se.skl.tp.vp.certificate.SenderIdExtractor;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.errorhandling.ExceptionUtil;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;

@Service
@Log4j2
public class HttpSenderIdExtractorProcessorImpl implements HttpSenderIdExtractorProcessor {

  private IPWhitelistHandler ipWhitelistHandler;
  private HeaderCertificateHelper headerCertificateHelper;
  private SenderIpExtractor senderIpExtractor;
  private String vpInstanceId;
  private ExceptionUtil exceptionUtil;
  @Value("${" + PropertyConstants.USE_HEADER_X_VP_AUTH_DN_TO_RETRIEVE_SENDER_ID + "}")
  private static boolean useHeaderXVpAuthDnToRetrieveSenderId;
  private String subjectPattern;
  private SenderIdExtractor senderIdExtractor;

  @Autowired
  public HttpSenderIdExtractorProcessorImpl(Environment env,
      SenderIpExtractor senderIpExtractor,
      HeaderCertificateHelper headerCertificateHelper,
      IPWhitelistHandler ipWhitelistHandler,
      ExceptionUtil exceptionUtil) {
    this.headerCertificateHelper = headerCertificateHelper;
    this.ipWhitelistHandler = ipWhitelistHandler;
    this.senderIpExtractor = senderIpExtractor;
    vpInstanceId = env.getProperty(PropertyConstants.VP_INSTANCE_ID);
    this.exceptionUtil = exceptionUtil;
    subjectPattern = env.getProperty(PropertyConstants.CERTIFICATE_SENDERID_SUBJECT_PATTERN);
    senderIdExtractor = new SenderIdExtractor(subjectPattern);
  }

  public static void setUseHeaderXVpAuthDnToRetrieveSenderId(boolean val) {
    useHeaderXVpAuthDnToRetrieveSenderId = val;
  }

  public static boolean isUseHeaderXVpAuthDnToRetrieveSenderId() {
    return useHeaderXVpAuthDnToRetrieveSenderId;
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    Message message = exchange.getIn();
    String callerRemoteAddress = senderIpExtractor.getCallerRemoteAddress(message);
    checkCallerOnWhitelist(callerRemoteAddress, senderIpExtractor.getCallerRemoteAddressHeaderName());

    String forwardedForIpAdress = senderIpExtractor.getForwardedForAddress(message);
    String senderIpAdress = forwardedForIpAdress != null ? forwardedForIpAdress : callerRemoteAddress;
    exchange.setProperty(VPExchangeProperties.SENDER_IP_ADRESS, senderIpAdress);

    String senderId = message.getHeader(HttpHeaders.X_VP_SENDER_ID, String.class);
    String senderVpInstanceId = message.getHeader(HttpHeaders.X_VP_INSTANCE_ID, String.class);
    if (senderId != null && vpInstanceId.equals(senderVpInstanceId)) {
      log.debug("Internal plattform call, setting senderId from property {}:{}", HttpHeaders.X_VP_SENDER_ID, senderId);
      checkCallerOnWhitelist(forwardedForIpAdress, senderIpExtractor.getForwardForHeaderName());
      exchange.setProperty(VPExchangeProperties.SENDER_ID, senderId);
    } else {
      senderId = null;
      if (useHeaderXVpAuthDnToRetrieveSenderId) {
        if (exchange.getIn().getHeader(HttpHeaders.DN_IN_CERT_FROM_REVERSE_PROXY, String.class) != null) {
          String principal = exchange.getIn().getHeader(HttpHeaders.DN_IN_CERT_FROM_REVERSE_PROXY, String.class);
          if (principal != null) {
            senderId = senderIdExtractor.extractSenderFromPrincipal(principal);
            log.debug("Getting senderId from header {}:{}", HttpHeaders.DN_IN_CERT_FROM_REVERSE_PROXY, senderId);
          }
        }
      }
      if (senderId == null) {
        senderId = getSenderIdFromCertificate(message);
        log.debug("Try to extract senderId from provided certificate. SenderId was {}", senderId);
      }
      exchange.setProperty(VPExchangeProperties.SENDER_ID, senderId);
    }
  }

  private String getSenderIdFromCertificate(Message message) {
    Object certificate = message.getHeader(HttpHeaders.CERTIFICATE_FROM_REVERSE_PROXY);
    return headerCertificateHelper.getSenderIDFromHeaderCertificate(certificate);
  }

  private void checkCallerOnWhitelist(String senderIpAdress, String header) {
    if (senderIpAdress != null && !ipWhitelistHandler.isCallerOnWhiteList(senderIpAdress)) {
      throw exceptionUtil.createVpSemanticException(VpSemanticErrorCodeEnum.VP011,
          " IP-address: " + senderIpAdress
              + ". HTTP header that caused checking: " + header);
    }
  }

}
