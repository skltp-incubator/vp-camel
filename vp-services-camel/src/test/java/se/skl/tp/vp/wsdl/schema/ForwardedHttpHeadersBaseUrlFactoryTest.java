package se.skl.tp.vp.wsdl.schema;

import static org.junit.Assert.*;
import static se.skl.tp.vp.wsdl.WsdlPathHelper.expandIfPrefixedClassPath;
import static se.skl.tp.vp.wsdl.pathinitialize.VirtualiseringsGeneratorValidation.MATCH_END_IN_COLON_SINGEL_DIGIT;
import static se.skl.tp.vp.wsdl.pathinitialize.VirtualiseringsGeneratorValidation.MATCH_XSD_FILES_NON_EXT;
import static se.skl.tp.vp.wsdl.pathinitialize.WsdlPathHelper.findFilesInDirectory;
import static se.skl.tp.vp.wsdl.pathinitialize.WsdlPathHelper.findFoldersInDirectory;

import com.sun.jndi.toolkit.url.Uri;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.vp.config.ProxyHttpForwardedHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.wsdl.pathinitialize.VirtualiseringGenerator;
import se.skl.tp.vp.wsdl.schema.ForwardedHttpHeadersBaseUrlFactory.BaseUrlModel;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProxyHttpForwardedHeaders.class)
public class ForwardedHttpHeadersBaseUrlFactoryTest {

  private static String PORT = "443";
  private static String HOST = "vp-loadbalancer-dns-name";
  private static String SCHEME = "http";
  private static String QUERY = "xsd=../../core_components/itintegration_registry_1.0.xsd";

  private static String ORIGINAL_URL =
      "https://test.esb.ntjp.se/vp/clinicalprocess/healthcond/certificate/GetCertificate/2/rivtabp21?wsdl";

  private static String ORIGINAL_URL_WITH_FRAGMENT =
      "https://test.esb.ntjp.se/vp/clinicalprocess/healthcond/certificate/GetCertificate/2/rivtabp21?wsdl#fragment";

  @Value("${" + PropertyConstants.WSDLFILES_DIRECTORY + "}")
  private String wsdlDir;

  @Autowired private ProxyHttpForwardedHeaders proxyHttpForwardedHeaders;

  @Test
  public void extractForwardedHttpHeadersForBaseUrl()
      throws MalformedURLException, URISyntaxException {
    URL uri = new URL(ORIGINAL_URL);
    BaseUrlModel baseUrlModel =
        new ForwardedHttpHeadersBaseUrlFactory()
            .extractForwardedHttpHeadersForBaseUrl(
                createMessageWithForwardedHeader(), proxyHttpForwardedHeaders, uri);
    assertTrue(baseUrlModel.host.equals(HOST));
    assertTrue(baseUrlModel.scheme.equals(SCHEME));
    assertTrue(baseUrlModel.port.equals(PORT));
  }

  @Test
  public void extractFromOriginalUrlInstead() throws MalformedURLException, URISyntaxException {
    URL uri = new URL(ORIGINAL_URL);
    BaseUrlModel baseUrlModel =
        new ForwardedHttpHeadersBaseUrlFactory()
            .extractForwardedHttpHeadersForBaseUrl(createMessage(), proxyHttpForwardedHeaders, uri);
    assertTrue(baseUrlModel.host.equals("test.esb.ntjp.se"));
    assertTrue(baseUrlModel.scheme.equals("https"));
    assertTrue(baseUrlModel.port.equals("-1"));
  }

  @Test
  public void expandByForwardedHeaders() throws MalformedURLException, URISyntaxException {
    URL uri = new URL(ORIGINAL_URL);
    BaseUrlModel baseUrlModel =
        new ForwardedHttpHeadersBaseUrlFactory()
            .extractForwardedHttpHeadersForBaseUrl(
                createMessageWithForwardedHeader(), proxyHttpForwardedHeaders, uri);
    Uri expandedUri =
        new Uri(
            new WsdlSchemaImportNodeHandler(null, null)
                .replaceBaseUrlParts(uri, baseUrlModel, QUERY));

    assertTrue(
        expandedUri.getHost().equals(HOST)
            && (expandedUri.getPort() == Integer.valueOf(PORT))
            && expandedUri.getScheme().equals(SCHEME)
            && expandedUri.getQuery().equals("?" + QUERY));
  }

  @Test
  public void expandByForwardedHeadersInkFragment()
      throws MalformedURLException, URISyntaxException {
    URL uri = new URL(ORIGINAL_URL_WITH_FRAGMENT);
    BaseUrlModel baseUrlModel =
        new ForwardedHttpHeadersBaseUrlFactory()
            .extractForwardedHttpHeadersForBaseUrl(
                createMessageWithForwardedHeader(), proxyHttpForwardedHeaders, uri);
    URL expandedUri =
        new URL(
            new WsdlSchemaImportNodeHandler(null, null)
                .replaceBaseUrlParts(uri, baseUrlModel, QUERY));

    assertTrue(
        expandedUri.getHost().equals(HOST)
            && (expandedUri.getPort() == Integer.valueOf(PORT))
            && expandedUri.toURI().getScheme().equals(SCHEME)
            && expandedUri.getQuery().equals(QUERY)
            && expandedUri.toURI().getFragment().equals("fragment"));
  }

  private Message createMessageWithForwardedHeader() {
    CamelContext ctx = new DefaultCamelContext();
    Exchange ex = new DefaultExchange(ctx);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getPort(), PORT);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getHost(), HOST);
    ex.getIn().setHeader(proxyHttpForwardedHeaders.getProto(), SCHEME);
    return ex.getIn();
  }

  private Message createMessage() {
    CamelContext ctx = new DefaultCamelContext();
    Exchange ex = new DefaultExchange(ctx);
    return ex.getIn();
  }

  @Test
  public void testVirtualiseringGenerator() throws IOException {
    VirtualiseringGenerator vtg = new VirtualiseringGenerator();
    String dir = expandIfPrefixedClassPath(wsdlDir);
    List<File> core_componentsFolders = findFoldersInDirectory(dir, "core_components");
    List<File> interactionFolder = findFoldersInDirectory(dir, ".*Interaction");
    List<File> ext = findFilesInDirectory(dir, ".*_ext.*\\.xsd");
    assertTrue(ext.size() == 2);
    assertTrue(core_componentsFolders.size() == 4);
    assertTrue(interactionFolder.size() == 10);
    vtg.generate(dir);
  }

  @Test
  public void testMATCH_XSD_FILES_NON_EXT() {
    assertTrue(
    testRegex(Arrays.asList(
        "SomeFileName.xsd",
        "SomeFileNamext.xsd",
        "ext_SomeFileName.xsd",
        "ext_SomeFileNameext_.xsd"),
        Arrays.asList(
            "SomeFileName_ext.xsd",
            "SomeFileName.xsdfault",
            "SomeFileName_ext.xsdfault",
            "SomeFileName_ext",
            "SomeFileName",
            "SomeFileName.xml",
            "Sext_omeFileName.xml"),
        MATCH_XSD_FILES_NON_EXT)
    );

  }

  @Test
  public void testMATCH_END_IN_COLON_SINGELE_DIGIT() {
    assertTrue(
        testRegex(Arrays.asList(
            "some:nice:version:1",
            ":7",
            "version:7",
            "som nice verion :8"),
            Arrays.asList(
                "bad:version:11",
                "bad:version:1:bad",
                "bad:version:1bad",
                ":12",
                ""),
            MATCH_END_IN_COLON_SINGEL_DIGIT)
    );
  }

  private boolean testRegex(List<String> matching,List<String> notMatchingCandidate,String regex){
    AtomicBoolean result = new AtomicBoolean(true);
    matching.forEach(candidate->{
      if(!candidate.matches(regex))
        result.set(false);

    });
    notMatchingCandidate.forEach(candidate->{
      if(!candidate.matches(regex))
        result.set(false);

    });
    return result.get();
  }
}
