package se.skl.tp.vp.httpheader;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.certificate.HeaderCertificateHelper;
import se.skl.tp.vp.constants.ApplicationProperties;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;

@Service
public class HttpHeaderExtractorProcessorImpl implements HttpHeaderExtractorProcessor {


    @Autowired
    IPWhitelistHandler ipWhitelistHandler;

    @Autowired
    HeaderCertificateHelper headerCertificateHelper;

    @Autowired
    SenderIpExtractor senderIpExtractor;

    @Value("${" + ApplicationProperties.VP_INSTANCE_ID + "}")
    String vpInstanceId;

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getIn();
        String senderId = message.getHeader(HttpHeaders.X_VP_SENDER_ID, String.class);
        String senderVpInstanceId = message.getHeader(HttpHeaders.X_VP_INSTANCE_ID, String.class);

        /*
         * Extract sender ip adress to session scope to be able to log in EventLogger.
         */
        String senderIpAdress = senderIpExtractor.extractSenderIpAdress(message);
        exchange.setProperty(VPExchangeProperties.SENDER_IP_ADRESS, senderIpAdress);

        if (senderId != null && vpInstanceId.equals(senderVpInstanceId)) {
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

            String senderIdFromHeaderCertificate = headerCertificateHelper.getSenderIDFromHeaderCertificate(certificate);
            exchange.setProperty(VPExchangeProperties.SENDER_ID, senderIdFromHeaderCertificate);
        }
    }

}
