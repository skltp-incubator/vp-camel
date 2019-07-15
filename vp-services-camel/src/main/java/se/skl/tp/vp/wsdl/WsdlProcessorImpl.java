package se.skl.tp.vp.wsdl;

import static se.skl.tp.vp.wsdl.PathHelper.expandIfPrefixedClassPath;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.config.ProxyHttpForwardedHeaders;
import se.skl.tp.vp.wsdl.schema.ForwardedHttpHeadersBaseUrlFactory;
import se.skl.tp.vp.wsdl.schema.ForwardedHttpHeadersBaseUrlFactory.BaseUrlModel;
import se.skl.tp.vp.wsdl.schema.WsdlSchemaImportPathExpander;

@Service
public class WsdlProcessorImpl implements WsdlProcessor {

  @Autowired private ProxyHttpForwardedHeaders proxyHttpForwardedHeaders;

  private ForwardedHttpHeadersBaseUrlFactory baseUrlFactory;

  private WsdlSchemaImportPathExpander expand;

  private WsdlConfiguration wsdlConfiguration;

  @Autowired
  public WsdlProcessorImpl(WsdlConfiguration wsdlConfiguration) {
    this.wsdlConfiguration = wsdlConfiguration;
    expand = new WsdlSchemaImportPathExpander();
    baseUrlFactory = new ForwardedHttpHeadersBaseUrlFactory();
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    String callURL = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);

    URL uri = new URL(callURL);

    String path = uri.getPath();

    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    WsdlConfig wsdlConfig = wsdlConfiguration.getOnWsdlUrl(path);

    if (wsdlConfig != null) {
      onValidWsdlRequest(exchange, uri, wsdlConfig);
    } else {
      onFaultyWsdlRequest(exchange);
    }
  }

  private void onFaultyWsdlRequest(Exchange exchange) {
    StringBuilder builder = new StringBuilder();
    builder.append("No wsdl found on this path, try any of the following paths:");
    for (String url : wsdlConfiguration.getAllWsdlUrl()) {
      builder.append(System.lineSeparator());
      builder.append(url);
    }
    exchange.getOut().setBody(builder.toString());
  }

  private void onValidWsdlRequest(Exchange exchange, URL uri, WsdlConfig wsdlConfig)
      throws DocumentException, IOException, URISyntaxException {
    exchange
        .getOut()
        .setBody(
            prepareWsdlBodyFromFileTemplate(exchange.getIn(), wsdlConfig.getWsdlfilepath(), uri));
  }

  private String prepareWsdlBodyFromFileTemplate(
      Message inComingMessage, String wsdlTemplateFilePath, URL uri)
      throws DocumentException, IOException, URISyntaxException {
    String template = openTemplate(wsdlTemplateFilePath);

    BaseUrlModel baseUrlModel =
        baseUrlFactory.extractForwardedHttpHeadersForBaseUrl(
            inComingMessage, proxyHttpForwardedHeaders, uri);

    return expand.allSchemaImports(template, baseUrlModel, uri);
  }

  private String openTemplate(String wsdlTemplateFilePath) throws IOException {
    return new String(
        Files.readAllBytes(Paths.get(expandIfPrefixedClassPath(wsdlTemplateFilePath))));
  }
}
