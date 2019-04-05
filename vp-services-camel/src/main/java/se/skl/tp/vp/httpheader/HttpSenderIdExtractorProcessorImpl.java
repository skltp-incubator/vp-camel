package se.skl.tp.vp.httpheader;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.netty4.NettyConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.certificate.HeaderCertificateHelper;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPConstants;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;
import se.skl.tp.vp.exceptions.VpSemanticException;

@Service
public class HttpSenderIdExtractorProcessorImpl implements HttpSenderIdExtractorProcessor {

    private static Logger LOGGER = LogManager.getLogger(HttpSenderIdExtractorProcessorImpl.class);

    private IPWhitelistHandler ipWhitelistHandler;
    private HeaderCertificateHelper headerCertificateHelper;
    private SenderIpExtractor senderIpExtractor;
    private String vpInstanceId;

    @Autowired
    public HttpSenderIdExtractorProcessorImpl(Environment env,
                                            SenderIpExtractor senderIpExtractor,
                                            HeaderCertificateHelper headerCertificateHelper,
                                            IPWhitelistHandler ipWhitelistHandler) {
        this.headerCertificateHelper = headerCertificateHelper;
        this.ipWhitelistHandler = ipWhitelistHandler;
        this.senderIpExtractor = senderIpExtractor;
        vpInstanceId = env.getProperty(PropertyConstants.VP_INSTANCE_ID);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getIn();
        String senderId = message.getHeader(HttpHeaders.X_VP_SENDER_ID, String.class);
        String senderVpInstanceId = message.getHeader(HttpHeaders.X_VP_INSTANCE_ID, String.class);
        //The original sender of the request, that might have been transferred by an RTjP. Can be null.
        String originalServiceconsumerHsaid = message.getHeader(VPExchangeProperties.ORIGINAL_SERVICE_CONSUMER_HSA_ID, String.class);

        /*
         * Extract sender ip address to session scope to be able to log in EventLogger.
         */
        String senderIpAdress = senderIpExtractor.extractSenderIpAdress(message);
        exchange.setProperty(VPExchangeProperties.SENDER_IP_ADRESS, senderIpAdress);
        boolean isOnWhitelist = ipWhitelistHandler.isCallerOnWhiteList(senderIpAdress);

        if (originalServiceconsumerHsaid != null && !originalServiceconsumerHsaid.trim().isEmpty()) {
            exchange.setProperty(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid);
            exchange.getOut().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, originalServiceconsumerHsaid);
        } else {
            exchange.setProperty(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, senderId);
            exchange.getOut().setHeader(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, senderId);
        }

        if (senderId != null && vpInstanceId.equals(senderVpInstanceId)) {
            LOGGER.debug("Yes, sender id extracted from inbound property {}: {}, check whitelist!", HttpHeaders.X_VP_SENDER_ID, senderId);
            /*
             * x-vp-sender-id exist as inbound property and x-vp-instance-id macthes this VP instance, a mandatory check against the whitelist of
             * ip addresses is needed. VPUtil.checkCallerOnWhiteList throws VpSemanticException in case ip address is not in whitelist.
             */
            if(!ipWhitelistHandler.isCallerOnWhiteList(senderIpAdress)){
                throw new VpSemanticException(VpSemanticErrorCodeEnum.VP011.getCode()
                        + " IP-address: " + senderIpAdress
                        + ". HTTP header that caused checking: " + NettyConstants.NETTY_REMOTE_ADDRESS,
                        VpSemanticErrorCodeEnum.VP011);
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
