package se.skl.tp.vp.httpheader;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.netty4.NettyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.certificate.PemConverter;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.constants.HttpHeaders;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HttpHeaderExtractorProcessorImpl implements HttpHeaderExtractorProcessor {

    @Autowired
    IPWhitelistHandler ipWhitelistHandler;

    @Autowired
    Pattern certificateSenderIDPattern;

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getIn();
        String senderId = message.getHeader(HttpHeaders.X_VP_SENDER_ID, String.class);
        String senderVpInstanceId = message.getHeader(HttpHeaders.X_VP_INSTANCE_ID, String.class);

        /*
         * Extract sender ip adress to session scope to be able to log in EventLogger.
         */
        String senderIpAdress = extractSenderIpAdress(message);
        exchange.setProperty(VPExchangeProperties.SENDER_IP_ADRESS, senderIpAdress);

        if (senderId != null /*&& vpInstanceId.equals(senderVpInstanceId)*/) {
            //log.debug("Yes, sender id extracted from inbound property {}: {}, check whitelist!", HttpHeaders.X_VP_SENDER_ID, senderId);

            /*
             * x-vp-sender-id exist as inbound property and x-vp-instance-id macthes this VP instance, a mandatory check against the whitelist of
             * ip addresses is needed. VPUtil.checkCallerOnWhiteList throws VpSemanticException in case ip address is not in whitelist.
             */
            if(!ipWhitelistHandler.isCallerOnWhiteList(senderIpAdress)){
                //throw VPUtil.createVP011Exception(senderIpAdress, HttpHeaders.X_VP_SENDER_ID);
            }

            // Make sure the sender id is set in session scoped property for authorization and logging
            exchange.setProperty(VPExchangeProperties.SENDER_ID, senderId);

        } else {
            Object certificate = message.getHeader(HttpHeaders.REVERSE_PROXY_HEADER_NAME);

            try {
                if (isX509Certificate(certificate)) {
                    exchange.setProperty(VPExchangeProperties.SENDER_ID, extractFromX509Certificate(certificate));
                } else if (PemConverter.isPEMCertificate(certificate)) {
                    exchange.setProperty(VPExchangeProperties.SENDER_ID, extractFromPemFormatCertificate(certificate));
                } else {
                    /*log.error("Unkown certificate type found in httpheader: {}", HttpHeaders.REVERSE_PROXY_HEADER_NAME);
                    throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002
                            + " Exception, unkown certificate type found in httpheader "
                            + HttpHeaders.REVERSE_PROXY_HEADER_NAME,
                            VpSemanticErrorCodeEnum.VP002);*/
                }
            } catch (Exception e) {
                /*log.error("Error occured parsing certificate in httpheader: {}", HttpHeaders.REVERSE_PROXY_HEADER_NAME, e);
                throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002
                        + " Exception occured parsing certificate in httpheader "
                        + HttpHeaders.REVERSE_PROXY_HEADER_NAME, VpSemanticErrorCodeEnum.VP002);*/
            }
        }
    }

    private String extractSenderIpAdress(Message message) {
        InetSocketAddress inetSocketAddress = message.getHeader(NettyConstants.NETTY_REMOTE_ADDRESS, InetSocketAddress.class);
        String senderIpAdress = inetSocketAddress.getAddress().getHostAddress();

        if(senderIpAdress == null){
            //senderIpAdress = VPUtil.extractIpAddress(message);
        }
        return senderIpAdress;
    }

    private String extractFromPemFormatCertificate(Object certificate) throws CertificateException {
        X509Certificate x509Certificate = PemConverter.buildCertificate(certificate);
        return extractSenderIdFromCertificate(x509Certificate);
    }

    private String extractFromX509Certificate(Object certificate) {
        X509Certificate x509Certificate = (X509Certificate) certificate;
        return extractSenderIdFromCertificate(x509Certificate);
    }

    static boolean isX509Certificate(Object certificate) {
        if (certificate instanceof X509Certificate) {
            //log.debug("Found X509Certificate in httpheader: {}", HttpHeaders.REVERSE_PROXY_HEADER_NAME);
            return true;
        }
        return false;
    }

    private String extractSenderIdFromCertificate(final X509Certificate certificate) {

        //log.debug("Extracting sender id from certificate.");

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

            //log.debug("Found sender id: {}", senderId);
            return senderId.startsWith("#") ? this.convertFromHexToString(senderId.substring(5)) : senderId;
        } else {
            /*throw new VpSemanticException(VpSemanticErrorCodeEnum.VP002 + " No senderId found in Certificate: " + principalName,
                    VpSemanticErrorCodeEnum.VP002);*/
            return "";
        }
    }
}
