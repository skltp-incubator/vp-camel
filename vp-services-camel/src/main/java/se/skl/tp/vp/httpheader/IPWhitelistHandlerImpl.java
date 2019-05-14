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

    private static Logger LOGGER = LogManager.getLogger(IPWhitelistHandlerImpl.class);

    private String [] whiteListArray;

    private String [] consumerListArray;

    @Autowired
    public IPWhitelistHandlerImpl(@Value("${" + PropertyConstants.IP_WHITELIST + "}") String whitelistString,
                                  @Value("${" + PropertyConstants.IP_CONSUMER_LIST + "}") String consumerlistString) {
        if(whitelistString != null && !whitelistString.isEmpty()) {
            whiteListArray = whitelistString.split(",");
        }
        if(consumerlistString != null && !consumerlistString.isEmpty()) {
            consumerListArray = consumerlistString.split(",");
        }
    }

    @Override
    public boolean isCallerOnConsumerList(String senderIpAdress) {
        LOGGER.debug("Check if caller {} is in consumer list before using HTTP header {}...",
                senderIpAdress, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
        if (consumerListArray == null) {
            LOGGER.debug("ConsumerList was NULL, so returning true. Might be changed...");
            //If list isn't configured we return true. Maybe install a switch instead..
            return true;
        }
        return isCallerOnList(senderIpAdress, consumerListArray, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, "Consumerlist");
    }
    @Override
    public boolean isCallerOnWhiteList(String senderIpAdress) {
        LOGGER.debug("Check if caller {} is in white list before using HTTP header {}...", senderIpAdress, NettyConstants.NETTY_REMOTE_ADDRESS);
        return isCallerOnList(senderIpAdress, whiteListArray, NettyConstants.NETTY_REMOTE_ADDRESS, "Whitelist");

    }

    private boolean isCallerOnList(String senderIpAdress, String[] list, String header, String listName) {

        //When no sender exist we can not validate against the list
        if (senderIpAdress == null || senderIpAdress.trim().isEmpty()) {
            LOGGER.warn("A potential empty ip address from the caller, ip adress is: {}. HTTP header that caused checking: {} ", senderIpAdress, header);
            return false;
        }


        //When no list exist we can not validate incoming ip address
        if (list == null) {
            LOGGER.warn("A check against the ip address in {} was requested, but the {} is configured empty. Update VP configuration for the {}", listName, listName, listName);
            return false;
        }


        for (String ipAddress : list) {
            if(senderIpAdress.trim().startsWith(ipAddress.trim())){
                LOGGER.debug("Caller matches ip address/subdomain in {}", listName);
                return true;
            }
        }
        String content = "";
        for (String s : list) {
            content += s + ", ";
        }

        LOGGER.warn("Caller was not on the {} of accepted IP-addresses. IP-address: {}, accepted IP-addresses in {}: {}", listName, senderIpAdress, listName, content);
        return false;
    }
}
