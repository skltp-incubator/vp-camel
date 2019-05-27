package se.skl.tp.vp.certificate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.Exchange;
import org.apache.camel.component.netty4.NettyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

@Service
@Log4j2
public class CertificateExtractorProcessorImpl implements CertificateExtractorProcessor {

  private Pattern certificateSenderIDPattern;

  @Autowired
  public CertificateExtractorProcessorImpl(
      @Value("${" + PropertyConstants.CERTIFICATE_SENDERID_SUBJECT_PATTERN + "}") String certificateSenderidSubject) {
    certificateSenderIDPattern = Pattern.compile(certificateSenderidSubject);
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    String name = ""+exchange.getIn().getHeader(NettyConstants.NETTY_SSL_CLIENT_CERT_SUBJECT_NAME, String.class);

    final Matcher matcher = certificateSenderIDPattern.matcher(name);

    if (matcher.find()) {
      final String senderId = matcher.group(1);
      log.debug("Found sender id: {}", senderId);
      String id = senderId.startsWith("#") ? this.convertFromHexToString(senderId.substring(5)) : senderId;
      exchange.setProperty(VPExchangeProperties.SENDER_ID, id);
    } else {
      throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002 + " No senderId found in Certificate: " + name,
          VpSemanticErrorCodeEnum.VP002);
    }
  }

  private String convertFromHexToString(final String hexString) {
    byte[] txtInByte = new byte[hexString.length() / 2];
    int j = 0;
    for (int i = 0; i < hexString.length(); i += 2) {
      txtInByte[j++] = Byte.parseByte(hexString.substring(i, i + 2), 16);
    }
    return new String(txtInByte);
  }
}
