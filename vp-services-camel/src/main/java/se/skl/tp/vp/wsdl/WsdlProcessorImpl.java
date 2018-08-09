package se.skl.tp.vp.wsdl;

import com.sun.xml.internal.ws.api.server.SDDocument.WSDL;
import java.nio.file.Files;
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
    if(callURL.startsWith("/")){
      callURL = callURL.substring(1);
    }
    WsdlConfig wsdlConfig = wsdlConfiguration.getOnWsdlUrl(callURL);
    if (wsdlConfig != null) {
      Path path = Paths.get(wsdlConfig.getWsdlfilepath());
      exchange.getOut().setBody(Files.readAllBytes(path));
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
