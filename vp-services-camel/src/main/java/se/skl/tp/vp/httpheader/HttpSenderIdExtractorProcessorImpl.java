package se.skl.tp.vp.httpheader;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.netty4.NettyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.certificate.HeaderCertificateHelper;
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
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    Message message = exchange.getIn();
    String senderId = message.getHeader(HttpHeaders.X_VP_SENDER_ID, String.class);
    String senderVpInstanceId = message.getHeader(HttpHeaders.X_VP_INSTANCE_ID, String.class);

    /*
     * Extract sender ip address to session scope to be able to log in EventLogger.
     */
    String senderIpAdress = senderIpExtractor.extractSenderIpAdress(message);
    exchange.setProperty(VPExchangeProperties.SENDER_IP_ADRESS, senderIpAdress);

    if (senderId != null && vpInstanceId.equals(senderVpInstanceId)) {
      log.debug("Yes, sender id extracted from inbound property {}: {}, check whitelist!", HttpHeaders.X_VP_SENDER_ID, senderId);
      /*
       * x-vp-sender-id exist as inbound property and x-vp-instance-id matches this VP instance, a mandatory check against the whitelist of
       * ip addresses is needed. VPUtil.checkCallerOnWhiteList throws VpSemanticException in case ip address is not in whitelist.
       */
      if (!ipWhitelistHandler.isCallerOnWhiteList(senderIpAdress)) {
        throw exceptionUtil.createVpSemanticException(VpSemanticErrorCodeEnum.VP011,
            " IP-address: " + senderIpAdress
                + ". HTTP header that caused checking: " + NettyConstants.NETTY_REMOTE_ADDRESS);
      }

      // Make sure the sender id is set in session scoped property for authorization and logging
      exchange.setProperty(VPExchangeProperties.SENDER_ID, senderId);

    } else {
      Object certificate = message.getHeader(HttpHeaders.REVERSE_PROXY_HEADER_NAME);

      String senderIdFromHeaderCertificate = headerCertificateHelper.getSenderIDFromHeaderCertificate(certificate);
      exchange.setProperty(VPExchangeProperties.SENDER_ID, senderIdFromHeaderCertificate);
    }
  }

}
