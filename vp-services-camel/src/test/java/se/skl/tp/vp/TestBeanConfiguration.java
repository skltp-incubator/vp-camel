package se.skl.tp.vp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import se.skl.tp.vp.certificate.CertificateExtractorProcessor;
import se.skl.tp.vp.certificate.CertificateExtractorProcessorImpl;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.errorhandling.ExceptionMessageProcessor;
import se.skl.tp.vp.errorhandling.ExceptionMessageProcessorImpl;
import se.skl.tp.vp.httpheader.*;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.requestreader.RequestReaderProcessorXMLEventReader;

@TestConfiguration
@ComponentScan(basePackages = {"se.skl.tp.vp.errorhandling", "se.skltp.takcache", "se.skl.tp.hsa.cache"})
public class TestBeanConfiguration {
  @Autowired private Environment environment;

  @Bean(name = "requestReaderProcessor")
  public RequestReaderProcessor requestReaderProcessor() {
    return new RequestReaderProcessorXMLEventReader();
  }

  @Bean(name = "certificateExtractorProcessor")
  public CertificateExtractorProcessor certificateExtractorProcessor() {
    return new CertificateExtractorProcessorImpl(
        environment.getProperty(PropertyConstants.CERTIFICATE_SENDERID_SUBJECT_PATTERN));
  }

  @Bean
  public OutHeaderProcessor headerProcessor() {
    return new OutHeaderProcessorImpl();
  }

  @Bean
  public IPWhitelistHandler ipWhitelistHandler() {
    return new IPWhitelistHandlerImpl(environment.getProperty(PropertyConstants.IP_WHITELIST));
  }

  @Bean
  public IPWhitelistHandler emptyIpWhitelistHandler() {
    return new IPWhitelistHandlerImpl(null);
  }

  @Bean
  public CheckSenderAllowedToUseHeader checkSenderIdAgainstList() {
    return new CheckSenderAllowedToUseHeaderImpl(environment.getProperty(PropertyConstants.SENDER_ID_ALLOWED_LIST));
  }

  @Bean
  public CheckSenderAllowedToUseHeader emptyCheckSenderIdAgainstList() {
    return new CheckSenderAllowedToUseHeaderImpl(null);
  }

  @Bean
  public OriginalConsumerIdProcessorImpl originalConsumerIdProcessor() {
    return new OriginalConsumerIdProcessorImpl();
  }

  @Bean
  ExceptionMessageProcessor exceptionMessageProcessor() {
    return new ExceptionMessageProcessorImpl();
  }

  @Bean
  public SenderIpExtractor senderIpExtractor() {
    return new SenderIpExtractorFromHeader(
        environment.getProperty(PropertyConstants.VAGVALROUTER_SENDER_IP_ADRESS_HTTP_HEADER));
  }
}
