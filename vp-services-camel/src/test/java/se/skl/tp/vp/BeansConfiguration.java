package se.skl.tp.vp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import se.skl.tp.vp.certificate.*;
import se.skl.tp.vp.constants.ApplicationProperties;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.requestreader.RequestReaderProcessorXMLEventReader;

import java.util.regex.Pattern;

@Configuration
public class BeansConfiguration
{
    @Autowired
    private Environment environment;

    @Bean(name = "requestReaderProcessor")
    public RequestReaderProcessor requestReaderProcessor()
    {
        return new RequestReaderProcessorXMLEventReader();
    }

    @Bean(name = "certificateExtractorProcessor")
    public CertificateExtractorProcessor certificateExtractorProcessor()
    {
        return new CertificateExtractorProcessorImpl();
    }

    @Bean
    public Pattern certificateSenderIDPattern() {
        return Pattern.compile(environment.getProperty(ApplicationProperties.CERTIFICATE_SENDERID_SUBJECT)+CertificateSenderIDPatternConfig.CERT_SENDERID_PATTERN);
    }

    @Bean
    public SenderIpExtractor senderIpExtractor() {
        return new SenderIpExtractorFromHeader();
    }
}
