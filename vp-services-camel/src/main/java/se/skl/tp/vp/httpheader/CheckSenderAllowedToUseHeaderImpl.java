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
  private String senderIdString;

  private String senderIdNull = "The sender was null/empty. Could not check address in list {}. HTTP header that caused checking: {}.";
  private String listNull = "{} was NULL, so nothing to compare sender {} against. HTTP header that caused checking: {}.";
  private String listEmpty = "A check for sender {} against the ip address in {} was requested, but the list is configured empty.\n"
          + "Update the list OR set flag sender.id.check.enforce to false.\n" +
          "HTTP header that caused checking: {}.";
  private String senderIdOnList = "Caller {} matches ip address/subdomain in {}";
  private String senderIdNotOnList = "SenderId was not on the list {}. SenderId: {}, accepted senderId's in {}: <{}>";

  @Autowired
  public CheckSenderAllowedToUseHeaderImpl(@Value("${" + PropertyConstants.SENDER_ID_ALLOWED_LIST + ":#{null}}") String senderAllowedList) {

    if (!StringUtils.isEmpty(senderAllowedList)) {
      senderIdArray = senderAllowedList.split(",");
      senderIdString = senderAllowedList;
    }
  }

  /**
   * This method check's whether the caller has the right to use the header
   * X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID.
   */
  public boolean isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader(String senderId) {

    if (senderId == null || senderId.trim().isEmpty()) {
      LOGGER.warn(senderIdNull, PropertyConstants.SENDER_ID_ALLOWED_LIST, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    if (senderIdArray == null) {
      LOGGER.warn(listNull, PropertyConstants.SENDER_ID_ALLOWED_LIST, senderId, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    if (senderIdArray.length == 0) {
      LOGGER.warn(listEmpty, senderId, PropertyConstants.SENDER_ID_ALLOWED_LIST, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    for (String ipAddress : senderIdArray) {
      if (senderId.trim().startsWith(ipAddress.trim())) {
        LOGGER.info(senderIdOnList, senderId, PropertyConstants.SENDER_ID_ALLOWED_LIST);
        return true;
      }
    }
    LOGGER.warn(senderIdNotOnList, PropertyConstants.SENDER_ID_ALLOWED_LIST, senderId, PropertyConstants.SENDER_ID_ALLOWED_LIST, senderIdString);
    return false;
  }
}
