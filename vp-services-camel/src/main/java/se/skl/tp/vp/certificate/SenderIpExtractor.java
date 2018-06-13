package se.skl.tp.vp.certificate;

import org.apache.camel.Message;

public interface SenderIpExtractor {
    String extractSenderIpAdress(Message message);
}
