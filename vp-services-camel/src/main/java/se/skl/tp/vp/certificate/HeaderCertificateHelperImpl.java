package se.skl.tp.vp.certificate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.ApplicationProperties;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPConstants;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HeaderCertificateHelperImpl implements HeaderCertificateHelper {

    private static Logger LOGGER = LogManager.getLogger(HeaderCertificateHelperImpl.class);

    private Pattern certificateSenderIDPattern;

    @Autowired
    public HeaderCertificateHelperImpl(Environment env) {
        certificateSenderIDPattern = Pattern.compile(env.getProperty(ApplicationProperties.CERTIFICATE_SENDERID_SUBJECT)+VPConstants.CERT_SENDERID_PATTERN);
    }

    public String getSenderIDFromHeaderCertificate(Object certificate) {
        try {
            if (isX509Certificate(certificate)) {
                return extractFromX509Certificate(certificate);
            } else if (PemConverter.isPEMCertificate(certificate)) {
                return extractFromPemFormatCertificate(certificate);
            } else {
                LOGGER.error("Unkown certificate type found in httpheader: {}", HttpHeaders.REVERSE_PROXY_HEADER_NAME);
                    throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002
                            + " Exception, unkown certificate type found in httpheader "
                            + HttpHeaders.REVERSE_PROXY_HEADER_NAME,
                            VpSemanticErrorCodeEnum.VP002);
            }
        } catch (Exception e) {
            LOGGER.error("Error occured parsing certificate in httpheader: {}", HttpHeaders.REVERSE_PROXY_HEADER_NAME, e);
                throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002
                        + " Exception occured parsing certificate in httpheader "
                        + HttpHeaders.REVERSE_PROXY_HEADER_NAME, VpSemanticErrorCodeEnum.VP002);
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
            LOGGER.debug("Found X509Certificate in httpheader: {}", HttpHeaders.REVERSE_PROXY_HEADER_NAME);
            return true;
        }
        return false;
    }

    private String extractSenderIdFromCertificate(final X509Certificate certificate) {

        LOGGER.debug("Extracting sender id from certificate.");

        if (certificate == null) {
            throw new IllegalArgumentException("Cannot extract any sender because the certificate was null");
        }

        if (certificateSenderIDPattern == null) {
            throw new IllegalArgumentException("Cannot extract any sender becuase the pattern used to find it was null");
        }

        final String principalName = certificate.getSubjectX500Principal().getName();
        return extractSenderFromPrincipal(principalName);
    }

    private String convertFromHexToString(final String hexString) {
        byte[] txtInByte = new byte[hexString.length() / 2];
        int j = 0;
        for (int i = 0; i < hexString.length(); i += 2) {
            txtInByte[j++] = Byte.parseByte(hexString.substring(i, i + 2), 16);
        }
        return new String(txtInByte);
    }

    private String extractSenderFromPrincipal(String principalName) {
        final Matcher matcher = certificateSenderIDPattern.matcher(principalName);

        if (matcher.find()) {
            final String senderId = matcher.group(1);

            LOGGER.debug("Found sender id: {}", senderId);
            return senderId.startsWith("#") ? this.convertFromHexToString(senderId.substring(5)) : senderId;
        } else {
            throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002 + " No senderId found in Certificate: " + principalName,
                    VpSemanticErrorCodeEnum.VP002);
        }
    }
}
