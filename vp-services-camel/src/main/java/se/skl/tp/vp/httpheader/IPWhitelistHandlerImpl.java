package se.skl.tp.vp.httpheader;

import org.apache.camel.component.netty4.NettyConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;

@Service
public class IPWhitelistHandlerImpl implements IPWhitelistHandler{

    private static Logger logger = LogManager.getLogger(IPWhitelistHandlerImpl.class);

    private String [] whiteListArray;

    private String [] consumerListArray;

    @Autowired
    public IPWhitelistHandlerImpl(@Value("${" + PropertyConstants.IP_WHITELIST + ":#{null}}") String whitelistString,
                                  @Value("${" + PropertyConstants.IP_CONSUMER_LIST + ":#{null}}") String consumerlistString) {
        if(whitelistString != null && !whitelistString.isEmpty()) {
            whiteListArray = whitelistString.split(",");
        }
        if(consumerlistString != null && !consumerlistString.isEmpty()) {
            consumerListArray = consumerlistString.split(",");
        }
    }

    /**
     * This method check's whether the caller has the right to use the header X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID.
     */
    @Override
    public boolean isCallerOnConsumerList(String senderIpAdress) {
        logger.debug("Check if caller {} is in consumer list before using HTTP header {}...",
                senderIpAdress, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
        return isCallerOnList(senderIpAdress, consumerListArray, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, "Consumerlist");
    }

    @Override
    public boolean isCallerOnWhiteList(String senderIpAdress) {
        logger.debug("Check if caller {} is in white list before using HTTP header {}...", senderIpAdress, NettyConstants.NETTY_REMOTE_ADDRESS);
        return isCallerOnList(senderIpAdress, whiteListArray, NettyConstants.NETTY_REMOTE_ADDRESS, "Whitelist");
    }

    private boolean isCallerOnList(String senderIpAdress, String[] list, String header, String listName) {

        //When no sender exist we can not validate against the list
        if (senderIpAdress == null || senderIpAdress.trim().isEmpty()) {
            logger.warn("A potential empty ip address from the caller, ip adress is: {}. HTTP header that caused checking: {} ", senderIpAdress, header);
            return false;
        }

        if (list == null) {
            logger.debug(listName + " was NULL, so nothing to compare sender {} against. Returning false...", senderIpAdress);
            return false;
        }

        //When no list exist we can not validate incoming ip address
        if (list.length == 0) {
            logger.warn("A check for sender {} against the ip address in {} was requested, but the {} is configured empty. Update VP configuration for the {}", senderIpAdress, listName, listName, listName);
            return false;
        }


        for (String ipAddress : list) {
            if(senderIpAdress.trim().startsWith(ipAddress.trim())){
                logger.debug("Caller {} matches ip address/subdomain in {}", senderIpAdress, listName);
                return true;
            }
        }
        StringBuilder content = new StringBuilder();
        for (String s : list) {

            content.append(s + ",");
        }
        logger.warn("Caller was not on the {} of accepted IP-addresses. IP-address: {}, accepted IP-addresses in {}: {}", listName, senderIpAdress, listName, content.toString());
        return false;
    }
}
