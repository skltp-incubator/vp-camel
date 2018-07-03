package se.skl.tp.vp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.hsa.cache.HsaCacheImpl;
import se.skl.tp.vp.certificate.*;
import se.skl.tp.vp.constants.ApplicationProperties;
import se.skl.tp.vp.errorhandling.ExceptionMessageProcessor;
import se.skl.tp.vp.errorhandling.ExceptionMessageProcessorImpl;
import se.skl.tp.vp.httpheader.IPWhitelistHandler;
import se.skl.tp.vp.httpheader.IPWhitelistHandlerImpl;
import se.skl.tp.vp.httpheader.SenderIpExtractor;
import se.skl.tp.vp.httpheader.SenderIpExtractorFromHeader;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.requestreader.RequestReaderProcessorXMLEventReader;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.TakCacheImpl;

import java.util.regex.Pattern;

import static se.skl.tp.vp.constants.ApplicationProperties.HSA_FILES;

@Configuration
@ComponentScan(basePackages = {"se.skltp.takcache", "se.skl.tp.hsa.cache"})
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
        return new CertificateExtractorProcessorImpl(environment);
    }

    @Bean
    public IPWhitelistHandler ipWhitelistHandler() {
        return new IPWhitelistHandlerImpl(environment);
    }

    /*@Bean
    public Pattern certificateSenderIDPattern() {
        return Pattern.compile(environment.getProperty(ApplicationProperties.CERTIFICATE_SENDERID_SUBJECT)+CertificateExtractorProcessorImpl.CERT_SENDERID_PATTERN);
    }*/

    @Bean
    ExceptionMessageProcessor exceptionMessageProcessor() {
        return new ExceptionMessageProcessorImpl();
    }

    @Bean
    public SenderIpExtractor senderIpExtractor() {
        return new SenderIpExtractorFromHeader(environment);
    }
}
