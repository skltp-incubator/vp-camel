package se.skl.tp.vp.sslcontext;

import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SSLContextParametersConfig  {

    @Autowired
    Environment env;

    @Bean
    public SSLContextParameters incomingSSLContextParameters() {
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(env.getProperty("TP_TLS_STORE_LOCATION") + env.getProperty("TP_TLS_STORE_PRODUCER_FILE"));
        ksp.setPassword(env.getProperty("TP_TLS_STORE_PRODUCER_PASSWORD"));
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyPassword(env.getProperty("TP_TLS_STORE_PRODUCER_KEY_PASSWORD"));
        kmp.setKeyStore(ksp);
        KeyStoreParameters tsp = new KeyStoreParameters();
        tsp.setResource(env.getProperty("TP_TLS_STORE_LOCATION") + env.getProperty("TP_TLS_STORE_TRUSTSTORE_FILE"));
        tsp.setPassword(env.getProperty("TP_TLS_STORE_TRUSTSTORE_PASSWORD"));
        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(tsp);
        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setTrustManagers(tmp);
        return sslContextParameters;
    }

    @Bean
    public SSLContextParameters outgoingSSLContextParameters() {
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(env.getProperty("TP_TLS_STORE_LOCATION") + env.getProperty("TP_TLS_STORE_CONSUMER_FILE"));
        ksp.setPassword(env.getProperty("TP_TLS_STORE_CONSUMER_PASSWORD"));
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyPassword(env.getProperty("TP_TLS_STORE_CONSUMER_KEY_PASSWORD"));
        kmp.setKeyStore(ksp);
        KeyStoreParameters tsp = new KeyStoreParameters();
        tsp.setResource(env.getProperty("TP_TLS_STORE_LOCATION") + env.getProperty("TP_TLS_STORE_TRUSTSTORE_FILE"));
        tsp.setPassword(env.getProperty("TP_TLS_STORE_TRUSTSTORE_PASSWORD"));
        TrustManagersParameters tmp = new TrustManagersParameters();
        tmp.setKeyStore(tsp);
        SSLContextParameters sslContextParameters = new SSLContextParameters();
        sslContextParameters.setKeyManagers(kmp);
        sslContextParameters.setTrustManagers(tmp);
        return sslContextParameters;
    }

}
