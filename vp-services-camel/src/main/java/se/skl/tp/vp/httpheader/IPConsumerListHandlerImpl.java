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

  private static Logger LOGGER = LogManager.getLogger(IPWhitelistHandlerImpl.class);
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

    if (senderIpAdress == null || senderIpAdress.trim().isEmpty()) {
      LOGGER.warn(
          "The sender was null/empty. Could not check address in list {}. HTTP header that caused checking: {} ",
          PropertyConstants.IP_CONSUMER_LIST,
          HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    if (consumerListArray == null) {
      LOGGER.warn(
          "{} was NULL, so nothing to compare sender {} against. Returning false...",
          PropertyConstants.IP_CONSUMER_LIST,
          senderIpAdress);
      return false;
    }

    if (consumerListArray.length == 0) {
      LOGGER.warn(
          "A check for sender {} against the ip address in {} was requested, but the list is configured empty.\n"
              + "Update the list OR set flag ip.consumer.list.use to false.",
          senderIpAdress,
          PropertyConstants.IP_CONSUMER_LIST);
      return false;
    }

    for (String ipAddress : consumerListArray) {
      if (senderIpAdress.trim().startsWith(ipAddress.trim())) {
        LOGGER.info(
            "Caller {} matches ip address/subdomain in {}",
            senderIpAdress,
            PropertyConstants.IP_CONSUMER_LIST);
        return true;
      }
    }
    StringBuilder content = new StringBuilder();
    for (String s : consumerListArray) {
      content.append(s + ",");
    }
    content.deleteCharAt(content.length() - 1);
    LOGGER.warn(
        "Caller was not on the list {}. IP-address: {}, accepted IP-addresses in ConsumerList: <{}>",
        PropertyConstants.IP_CONSUMER_LIST,
        senderIpAdress,
        content.toString());
    return false;
  }
}
