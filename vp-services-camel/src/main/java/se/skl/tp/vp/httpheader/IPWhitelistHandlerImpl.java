package se.skl.tp.vp.httpheader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.ApplicationProperties;

@Service
public class IPWhitelistHandlerImpl implements IPWhitelistHandler{

    private String [] whiteListArray;

    @Autowired
    public IPWhitelistHandlerImpl(Environment env) {
        String whitelistString = env.getProperty(ApplicationProperties.IP_WHITELIST);

        if(whitelistString != null && !whitelistString.isEmpty()) {
            whiteListArray = whitelistString.split(",");
        }
    }

    @Override
    public boolean isCallerOnWhiteList(String senderIpAdress) {
        //log.debug("Check if caller {} is in white list berfore using HTTP header {}...", callerIp, httpHeader);

        //When no ip address exist we can not validate against whitelist
        if (senderIpAdress == null || senderIpAdress.trim().isEmpty()) {
            //log.warn("A potential empty ip address from the caller, ip adress is: {}. HTTP header that caused checking: {} ", callerIp, httpHeader);
            return false;
        }


        //When no whitelist exist we can not validate incoming ip address
        if (whiteListArray == null) {
            //log.warn("A check against the ip address whitelist was requested, but the whitelist is configured empty. Update VP configuration property IP_WHITE_LIST");
            return false;
        }


        for (String ipAddress : whiteListArray) {
            if(senderIpAdress.trim().startsWith(ipAddress.trim())){
                //log.debug("Caller matches ip address/subdomain in white list");
                return true;
            }
        }

        //log.warn("Caller was not on the white list of accepted IP-addresses. IP-address: {}, accepted IP-addresses in IP_WHITE_LIST: {}", callerIp, this.toString());
        return false;
    }
}
