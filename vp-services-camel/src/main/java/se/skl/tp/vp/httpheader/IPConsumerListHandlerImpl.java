package se.skl.tp.vp.httpheader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;

@Service
public class IPConsumerListHandlerImpl implements IPConsumerListHandler {

  private static Logger logger = LogManager.getLogger(IPWhitelistHandlerImpl.class);
  private String[] consumerListArray;

  @Autowired
  public IPConsumerListHandlerImpl(
      @Value("${" + PropertyConstants.IP_CONSUMER_LIST + ":#{null}}") String consumerlistString) {

    if (consumerlistString != null && !consumerlistString.isEmpty()) {
      consumerListArray = consumerlistString.split(",");
    }
  }

  /**
   * This method check's whether the caller has the right to use the header
   * X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID.
   */
  public boolean isCallerOnConsumerList(String senderIpAdress) {
    logger.debug(
        "Check if caller {} is in consumer list before using HTTP header {}...",
        senderIpAdress,
        HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);

    if (senderIpAdress == null || senderIpAdress.trim().isEmpty()) {
      logger.warn(
          "A potential empty ip address from the caller, ip adress is: {}. HTTP header that caused checking: {} ",
          senderIpAdress,
          HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    if (consumerListArray == null) {
      logger.debug(
          "ConsumerList was NULL, so nothing to compare sender {} against. Returning false...",
          senderIpAdress);
      return false;
    }

    if (consumerListArray.length == 0) {
      logger.warn(
          "A check for sender {} against the ip address in ConsumerList was requested, but the list is configured empty. Update VP configuration for the ConsumerList",
          senderIpAdress);
      return false;
    }

    for (String ipAddress : consumerListArray) {
      if (senderIpAdress.trim().startsWith(ipAddress.trim())) {
        logger.debug("Caller {} matches ip address/subdomain in ConsumerList", senderIpAdress);
        return true;
      }
    }
    StringBuilder content = new StringBuilder();
    for (String s : consumerListArray) {
      content.append(s + ",");
    }
    logger.warn(
        "Caller was not on the ConsumerList of accepted IP-addresses. IP-address: {}, accepted IP-addresses in ConsumerList: {}",
        senderIpAdress,
        content.toString());
    return false;
  }
}
