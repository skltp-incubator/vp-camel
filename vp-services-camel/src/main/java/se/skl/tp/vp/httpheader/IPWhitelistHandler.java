package se.skl.tp.vp.httpheader;

public interface IPWhitelistHandler {
    boolean isCallerOnWhiteList(String senderIpAdress);

    boolean isCallerOnConsumerList(String senderIpAdress);
}
