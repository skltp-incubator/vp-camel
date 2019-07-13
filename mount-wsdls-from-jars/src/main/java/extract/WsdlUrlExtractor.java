package extract;

import static testutil.VPStringUtil.inputStream2UTF8Str;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import xmlutil.XmlHelper;

class WsdlUrlExtractor {

  public enum WsdlurlExtraction {
    OK,
    ENTRY_NOT_FOUND,
    COULD_NOT_FIND_WSDL_URL_IN_ERNTRY
  }

  private String endPointAdress;
  private String folderName;
  private WsdlurlExtraction lastExcecutionResult;

  WsdlurlExtraction excecutionResult() {
    return lastExcecutionResult;
  }

  private XPath getEndPointAdress =
      XmlHelper.createXPath(
          "n:mule/n:flow/n:composite-source/https:inbound-endpoint/@address",
          "xsi=http://www.w3.org/2001/XMLSchema-instance",
          "cxf=http://www.mulesoft.org/schema/mule/cxf",
          "https=http://www.mulesoft.org/schema/mule/https",
          "http=http://www.mulesoft.org/schema/mule/http",
          "n=http://www.mulesoft.org/schema/mule/core",
          "spring=http://www.springframework.org/schema/beans");

  boolean excecutedOk(JarFile jarFile) throws IOException, DocumentException {

    JarEntry entry = jarFile.getJarEntry("tp2-service-mule-descriptor.xml");

    if (entry != null) {
      try(InputStream stream = jarFile.getInputStream(entry)){
        Document pSource =
            DocumentHelper.parseText(inputStream2UTF8Str(stream));
        org.dom4j.Node node = getEndPointAdress.selectSingleNode(pSource);
        lastExcecutionResult = extractFromNode(node);
      }
    } else {
      lastExcecutionResult = WsdlurlExtraction.ENTRY_NOT_FOUND;
    }
    return lastExcecutionResult == WsdlurlExtraction.OK;
  }

  private WsdlurlExtraction extractFromNode(Node node) {
    WsdlurlExtraction result ;
    if (node == null) {
      result = WsdlurlExtraction.COULD_NOT_FIND_WSDL_URL_IN_ERNTRY;
    } else {
      String tmp = node.getStringValue();

      String[] split = tmp.split("\\{TP_BASE_URI}/");
      if (split.length != 2) {
        result = WsdlurlExtraction.COULD_NOT_FIND_WSDL_URL_IN_ERNTRY;
        endPointAdress = null;
        folderName = null;
      }else{

        endPointAdress = split[1];
        split = endPointAdress.split("/");
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
          builder.append(s);
          builder.append(".");
        }
        folderName = builder.toString().substring(0, builder.toString().length() - 1);
        result = WsdlurlExtraction.OK;
      }
    }
    return result;
  }

  String wsdlUrl() {
    return endPointAdress;
  }

  Path subDir(String parentFolder) {
    return Paths.get(parentFolder, folderName);
  }
}
