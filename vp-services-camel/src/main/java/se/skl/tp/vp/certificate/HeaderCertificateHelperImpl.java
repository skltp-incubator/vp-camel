package se.skl.tp.vp.certificate;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

@Service
@Log4j2
public class HeaderCertificateHelperImpl implements HeaderCertificateHelper {

  private SenderIdExtractor senderIdExtractor;
  private static final String PATTERN_PROPERTY = "${" + PropertyConstants.CERTIFICATE_SENDERID_SUBJECT_PATTERN + "}";

  @Autowired
  public HeaderCertificateHelperImpl(@Value(PATTERN_PROPERTY) String certificateSenderId) {
    senderIdExtractor = new SenderIdExtractor(certificateSenderId);
  }

  public String getSenderIDFromHeaderCertificate(Object certificate) {
    String senderId = null;
    boolean isUnknownCertificateType = false;

    evaluateCertificateNotNull(certificate);

    try {
      if (isX509Certificate(certificate)) {
        senderId = extractFromX509Certificate(certificate);
      } else if (PemConverter.isPEMCertificate(certificate)) {
        senderId = extractFromPemFormatCertificate(certificate);
      } else {
        isUnknownCertificateType = true;
      }
    } catch (Exception e) {
      log.error("Error occured parsing certificate in httpheader: {}", HttpHeaders.REVERSE_PROXY_HEADER_NAME, e);
      throw createVP002Exception("Exception occured parsing certificate in httpheader "
          + HttpHeaders.REVERSE_PROXY_HEADER_NAME);
    }

    evaluateResult(senderId, isUnknownCertificateType);
    return senderId;
  }

  private void evaluateCertificateNotNull(Object certificate) {
    if (certificate == null) {
      throw createVP002Exception("No certificate found in httpheader "
          + HttpHeaders.REVERSE_PROXY_HEADER_NAME);
    }
  }

  private void evaluateResult(String senderId, boolean isUnknownCertificateType) {
    if (isUnknownCertificateType) {
      throw createVP002Exception("Exception, unkown certificate type found in httpheader "
          + HttpHeaders.REVERSE_PROXY_HEADER_NAME);

    } else if (senderId == null) {
      throw createVP002Exception("No senderId found in Certificate");
    }
  }

  private String extractFromPemFormatCertificate(Object certificate) throws CertificateException {
    X509Certificate x509Certificate = PemConverter.buildCertificate(certificate);
    return extractSenderIdFromCertificate(x509Certificate);
  }

  private String extractFromX509Certificate(Object certificate) {
    X509Certificate x509Certificate = (X509Certificate) certificate;
    return extractSenderIdFromCertificate(x509Certificate);
  }

  private static boolean isX509Certificate(Object certificate) {
    if (certificate instanceof X509Certificate) {
      log.debug("Found X509Certificate in httpheader: {}", HttpHeaders.REVERSE_PROXY_HEADER_NAME);
      return true;
    }
    return false;
  }

  private String extractSenderIdFromCertificate(final X509Certificate certificate) {
    log.debug("Extracting sender id from certificate.");
    final String principalName = certificate.getSubjectX500Principal().getName();
    return senderIdExtractor.extractSenderFromPrincipal(principalName);
  }

  private VpSemanticException createVP002Exception(String msg) {
    return new VpSemanticException(String.format("%s %s", VpSemanticErrorCodeEnum.VP002, msg)
        , VpSemanticErrorCodeEnum.VP002);
  }
}
