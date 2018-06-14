package se.skl.tp.vp.httpheader;

import org.apache.camel.Message;

public interface SenderIpExtractor {
    String extractSenderIpAdress(Message message);
}
