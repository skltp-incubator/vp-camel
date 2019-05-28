package se.skl.tp.vp.httpheader;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;

/**
 * This class is used to check allowed senderId's when the property approve.to.use.header.original.consumer is set to true.
 * If that is the case, and if incoming request contains the header x-rivta-original-serviceconsumer-hsaid, the senderId must be
 * contained in the comma-separated property list sender.id.allowed.list
 */
@Service
public class CheckSenderAllowedToUseHeaderImpl implements CheckSenderAllowedToUseHeader {

  private static Logger LOGGER = LogManager.getLogger(CheckSenderAllowedToUseHeaderImpl.class);
  private String[] senderIdArray;

  public static String SENDER_ID_NULL = "The sender was null/empty. Could not check address in list {}. HTTP header that caused checking: {}.";
  public static String LIST_NULL = "{} was NULL, so nothing to compare sender {} against. HTTP header that caused checking: {}.";
  public static String LIST_EMPTY = "A check for sender {} against the ip address in {} was requested, but the list is configured empty.\n"
          + "Update the list OR set flag sender.id.check.enforce to false.\n" +
          "HTTP header that caused checking: {}.";
  public static String SENDER_ID_ON_LIST = "Caller {} matches ip address/subdomain in {}";
  public static String SENDER_ID_NOT_ON_LIST = "SenderId was not on the list {}. SenderId: {}, accepted senderId's in {}: <{}>";

  @Autowired
  public CheckSenderAllowedToUseHeaderImpl(
      @Value("${" + PropertyConstants.SENDER_ID_ALLOWED_LIST + ":#{null}}") String consumerlistString) {

    if (!StringUtils.isEmpty(consumerlistString)) {
      senderIdArray = consumerlistString.split(",");
    }
  }

  /**
   * This method check's whether the caller has the right to use the header
   * X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID.
   */
  public boolean isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader(String senderId) {

    if (senderId == null || senderId.trim().isEmpty()) {
      LOGGER.warn(SENDER_ID_NULL, PropertyConstants.SENDER_ID_ALLOWED_LIST, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    if (senderIdArray == null) {
      LOGGER.warn(LIST_NULL, PropertyConstants.SENDER_ID_ALLOWED_LIST, senderId, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    if (senderIdArray.length == 0) {
      LOGGER.warn(LIST_EMPTY, senderId, PropertyConstants.SENDER_ID_ALLOWED_LIST, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    for (String ipAddress : senderIdArray) {
      if (senderId.trim().startsWith(ipAddress.trim())) {
        LOGGER.info(SENDER_ID_ON_LIST, senderId, PropertyConstants.SENDER_ID_ALLOWED_LIST);
        return true;
      }
    }
    StringBuilder content = new StringBuilder();
    for (String s : senderIdArray) {
      content.append(s + ",");
    }
    content.deleteCharAt(content.length() - 1);
    LOGGER.warn(SENDER_ID_NOT_ON_LIST, PropertyConstants.SENDER_ID_ALLOWED_LIST, senderId,
            PropertyConstants.SENDER_ID_ALLOWED_LIST, content.toString());
    return false;
  }
}
