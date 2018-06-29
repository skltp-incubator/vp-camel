package se.skl.tp.vp.vagval.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static se.skl.tp.vp.constants.ApplicationProperties.DEFAULT_ROUTING_ADDRESS_DELIMITER;

public class DefaultRoutingUtil {

    private DefaultRoutingUtil() {
        //  Static utility
    }

    public static boolean useOldStyleDefaultRouting(String receiverAddress, String addressDelimiter){
        // Determine if delimiter is set and present in request logical address.
        // Delimiter is used in deprecated default routing (VG#VE).
        if( receiverAddress!=null && addressDelimiter!=null && !addressDelimiter.isEmpty() ){
            return receiverAddress.contains(addressDelimiter);
        }
        return false;
    }


    public static List<String> extractReceiverAdresses(String recieverAddressesString, String addressDelimiter) {
        List<String> receiverAddresses = new ArrayList<>();

        StringTokenizer strToken = new StringTokenizer(recieverAddressesString, addressDelimiter);
        while (strToken.hasMoreTokens()) {
            String tempAddress = (String) strToken.nextElement();
            if (!receiverAddresses.contains(tempAddress)) {
                receiverAddresses.add(0, tempAddress);
            }
        }
        return receiverAddresses;
    }
}