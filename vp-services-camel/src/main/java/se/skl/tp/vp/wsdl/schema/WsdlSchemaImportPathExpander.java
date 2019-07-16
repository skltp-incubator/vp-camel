package se.skl.tp.vp.wsdl.schema;

import static se.skl.tp.vp.xmlutil.XmlHelper.applyHandlingToNodes;
import static se.skl.tp.vp.xmlutil.XmlHelper.createXPath;

import java.net.URL;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;
import se.skl.tp.vp.wsdl.schema.ForwardedHttpHeadersBaseUrlFactory.BaseUrlModel;

public class WsdlSchemaImportPathExpander {

  private XPath getAlladressLocation =
      createXPath(
          "/wsdl:definitions/wsdl:service/wsdl:port/soap:address/@location",
          "soap=http://schemas.xmlsoap.org/wsdl/soap/",
          "wsdl=http://schemas.xmlsoap.org/wsdl/",
          "xsd=http://www.w3.org/2001/XMLSchema");

  private XPath getAllXsdImports =
      createXPath(
          "//xsd:import/@schemaLocation",
          "soap=http://schemas.xmlsoap.org/wsdl/soap/",
          "wsdl=http://schemas.xmlsoap.org/wsdl/",
          "xsd=http://www.w3.org/2001/XMLSchema");


  public String allSchemaImports(String wsdlSource, BaseUrlModel baseUrlModel, URL pOriginalUri)

      throws DocumentException {
    Document wsdl = DocumentHelper.parseText(wsdlSource);
    applyHandlingToNodes(
        wsdl, getAllXsdImports, new WsdlSchemaImportNodeHandler(pOriginalUri, baseUrlModel));
    applyHandlingToNodes(
        wsdl, getAlladressLocation, new WsdlAddressNodeHandler(pOriginalUri, baseUrlModel));

    return wsdl.asXML();
  }
}
