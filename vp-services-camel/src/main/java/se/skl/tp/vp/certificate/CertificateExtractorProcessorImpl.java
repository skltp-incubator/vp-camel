package se.skl.tp.vp.certificate;

import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.component.netty4.NettyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

@Service
@Log4j2
public class CertificateExtractorProcessorImpl implements CertificateExtractorProcessor {

  SenderIdExtractor senderIdExtractor;
  private static final String PATTERN_PROPERTY = "${" + PropertyConstants.CERTIFICATE_SENDERID_SUBJECT_PATTERN + "}";
  private static final String DN_PROPERTY = "${" + PropertyConstants.USE_HEADER_X_VP_AUTH_DN_TO_RETRIEVE_SENDER_ID + ":#{false}}";
  private boolean useHeaderXVpAuthDnToRetrieveSenderId;

  @Autowired
  public CertificateExtractorProcessorImpl(
          @Value(PATTERN_PROPERTY) String certificateSenderidSubject,
          @Value(DN_PROPERTY) Boolean use) {
    senderIdExtractor = new SenderIdExtractor(certificateSenderidSubject);
    useHeaderXVpAuthDnToRetrieveSenderId = use;
  }

  @Override
  public void process(Exchange exchange){
    String principal = "" + exchange.getIn().getHeader(NettyConstants.NETTY_SSL_CLIENT_CERT_SUBJECT_NAME, String.class);
    String senderId = null;
    if (useHeaderXVpAuthDnToRetrieveSenderId) {
      if (exchange.getIn().getHeader(HttpHeaders.DN_IN_CERT_FROM_REVERSE_PROXY, String.class) != null) {
        //TODO: senderId = getSenderIdFromDN(); ?
        senderId = exchange.getIn().getHeader(HttpHeaders.DN_IN_CERT_FROM_REVERSE_PROXY, String.class);
      }
    }
    if (senderId == null) {
      senderId = senderIdExtractor.extractSenderFromPrincipal(principal);
    }
    if (senderId == null) {
      throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002 + " No senderId found in Certificate: " + principal,
          VpSemanticErrorCodeEnum.VP002);
    }
    exchange.setProperty(VPExchangeProperties.SENDER_ID, senderId);
  }


}
