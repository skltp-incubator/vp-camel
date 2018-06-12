package se.skl.tp.vp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import se.skl.tp.vp.certificate.CertificateExtractorProcessor;
import se.skl.tp.vp.certificate.CertificateExtractorProcessorImpl;
import se.skl.tp.vp.certificate.CertificateSenderIDPatternConfig;
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
        return Pattern.compile(environment.getProperty("certificate.senderid.subject")+CertificateSenderIDPatternConfig.CERT_SENDERID_PATTERN);
    }
}
