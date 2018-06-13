package se.skl.tp.vp.certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import se.skl.tp.vp.constants.ApplicationProperties;

import java.util.regex.Pattern;

@Configuration
public class CertificateSenderIDPatternConfig {

    public static final String CERT_SENDERID_PATTERN = "=([^,]+)";

    @Autowired
    Environment env;

    @Bean
    public Pattern certificateSenderIDPattern() {
        return Pattern.compile(env.getProperty(ApplicationProperties.CERTIFICATE_SENDERID_SUBJECT)+CERT_SENDERID_PATTERN);
    }
}
