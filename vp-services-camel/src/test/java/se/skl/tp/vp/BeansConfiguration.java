package se.skl.tp.vp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import se.skl.tp.vp.requestreader.RequestReaderProcessor;
import se.skl.tp.vp.requestreader.RequestReaderProcessorXMLEventReader;

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
}
