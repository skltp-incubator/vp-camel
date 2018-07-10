package se.skl.tp.vp.wsdl;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
public class WsdlProcessorImpl implements WsdlProcessor {

    private WsdlConfiguration wsdlConfiguration;

    @Autowired
    public WsdlProcessorImpl(WsdlConfiguration wsdlConfiguration) {
        this.wsdlConfiguration = wsdlConfiguration;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String callURL = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
        WsdlConfig wsdlConfig = wsdlConfiguration.getOnWsdlUrl(callURL);
        if (wsdlConfig != null) {
            Path path = Paths.get(wsdlConfig.getWsdlfilepath());
            String stringFromFile = java.nio.file.Files.lines(path).collect(Collectors.joining());
            exchange.getOut().setBody(stringFromFile);
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("No wsdl found on this path, try any of the following paths:");
            for (String url : wsdlConfiguration.getAllWsdlUrl()) {
                builder.append(System.lineSeparator());
                builder.append(url);
            }
            exchange.getOut().setBody(builder.toString());
        }
    }
}
