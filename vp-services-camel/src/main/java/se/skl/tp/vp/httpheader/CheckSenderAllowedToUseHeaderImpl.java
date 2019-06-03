package se.skl.tp.vp.httpheader;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
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
@Log4j2
@Service
public class CheckSenderAllowedToUseHeaderImpl implements CheckSenderAllowedToUseHeader {

  private String[] senderIdArray;
  private String senderIdString;

  private static final String MSG_SENDERID_MISSING = "The sender was null/empty. Could not check address in list {}. HTTP header that caused checking: {}.";
  private static final String MSG_CONFIGURATION_MISSING = "{} was NULL, so nothing to compare sender {} against. HTTP header that caused checking: {}.";
  private static final String MSG_CONFIGURATION_EMPTY = "A check for sender {} against the ip address in {} was requested, but the list is configured empty.\n"
          + "Update the list OR set flag sender.id.check.enforce to false.\n" +
          "HTTP header that caused checking: {}.";
  private static final String MSG_SENDER_ALLOWED_SET_HEADER = "Sender {} allowed to set x-rivta-original-serviceconsumer-hsaid";
  private static final String MSG_SENDER_NOT_ALLOWED_SET_HEADER = "Sender {} not allowed to set x-rivta-original-serviceconsumer-hsaid, accepted senderId's in {}: [{}]";

  @Autowired
  public CheckSenderAllowedToUseHeaderImpl(@Value("${" + PropertyConstants.SENDER_ID_ALLOWED_LIST + ":#{null}}") String senderAllowedList) {

    if (!StringUtils.isEmpty(senderAllowedList)) {
      senderIdArray = senderAllowedList.split(",");
      senderIdString = senderAllowedList;
    }
  }

  public boolean isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader(String senderId) {

    if (senderId == null || senderId.trim().isEmpty()) {
      log.warn(MSG_SENDERID_MISSING, PropertyConstants.SENDER_ID_ALLOWED_LIST, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    if (senderIdArray == null) {
      log.warn(MSG_CONFIGURATION_MISSING, PropertyConstants.SENDER_ID_ALLOWED_LIST, senderId, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    if (senderIdArray.length == 0) {
      log.warn(MSG_CONFIGURATION_EMPTY, senderId, PropertyConstants.SENDER_ID_ALLOWED_LIST, HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
      return false;
    }

    for (String id : senderIdArray) {
      if (senderId.trim().startsWith(id.trim())) {
        log.info(MSG_SENDER_ALLOWED_SET_HEADER, senderId, PropertyConstants.SENDER_ID_ALLOWED_LIST);
        return true;
      }
    }
    log.warn(MSG_SENDER_NOT_ALLOWED_SET_HEADER, senderId, PropertyConstants.SENDER_ID_ALLOWED_LIST, senderIdString);
    return false;
  }
}
