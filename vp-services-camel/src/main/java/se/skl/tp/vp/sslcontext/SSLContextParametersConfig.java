package se.skl.tp.vp.sslcontext;

import org.apache.camel.util.jsse.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import se.skl.tp.vp.constants.ApplicationProperties;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SSLContextParametersConfig  {

    public static final String DELIMITER = ",";

    @Autowired
    Environment env;

    @Bean
    public SSLContextParameters incomingSSLContextParameters() {
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_FILE));
        ksp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_PASSWORD));
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_KEY_PASSWORD));
        kmp.setKeyStore(ksp);
        KeyStoreParameters tsp = new KeyStoreParameters();
        tsp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_FILE));
        tsp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_PASSWORD));
        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(tsp);
        SecureSocketProtocolsParameters sspp = new SecureSocketProtocolsParameters();
        String allowedIncomingProtocols = env.getProperty(ApplicationProperties.ALLOWED_INCOMING_PROTOCOLS);
        List<String> allowedProtocols = new ArrayList<>();
        for (String protocol: allowedIncomingProtocols.split(DELIMITER)) {
            if(!protocol.trim().isEmpty()){
                allowedProtocols.add(protocol);
            }
        }
        sspp.setSecureSocketProtocol(allowedProtocols);
        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setTrustManagers(tmp);
        sslContextParameters.setSecureSocketProtocols(sspp);
        return sslContextParameters;
    }

    @Bean
    public SSLContextParameters outgoingSSLContextParameters() {
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_FILE));
        ksp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_PASSWORD));
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_KEY_PASSWORD));
        kmp.setKeyStore(ksp);
        KeyStoreParameters tsp = new KeyStoreParameters();
        tsp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_FILE));
        tsp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_PASSWORD));
        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(tsp);
        SecureSocketProtocolsParameters sspp = new SecureSocketProtocolsParameters();
        String allowedOutgoingProtocols = env.getProperty(ApplicationProperties.ALLOWED_OUTGOING_PROTOCOLS);
        List<String> allowedProtocols = new ArrayList<>();
        for (String protocol: allowedOutgoingProtocols.split(",")) {
            if(!protocol.trim().isEmpty()){
                allowedProtocols.add(protocol);
            }
        }
        sspp.setSecureSocketProtocol(allowedProtocols);
        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setTrustManagers(tmp);
        sslContextParameters.setSecureSocketProtocols(sspp);
        return sslContextParameters;
    }

}
