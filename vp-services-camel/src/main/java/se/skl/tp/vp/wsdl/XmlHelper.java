package se.skl.tp.vp.wsdl;

import static se.skl.tp.vp.wsdl.WsdlPathHelper.expandIfPrefixedClassPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.xpath.DefaultXPath;
import org.jaxen.SimpleNamespaceContext;

public class XmlHelper {

  public static XPath createXPath(String pXpath, String... namespaces) {
    XPath xpath = new DefaultXPath(pXpath);
    setNameSpaces(namespaces, xpath);
    return xpath;
  }

  /**
   * Note that invalid xPaths, null arguments, source without matching element etc will result in
   * all kind of havoc.
   *
   * @param namespaces zero or more namespaces on the form ns0=http://my.magic.org
   * @return
   */
  public static String selectXPathStringValue(
      Document pSourceDocument, String pXpath, String... namespaces) {
    return createXPath(pXpath, namespaces).selectSingleNode(pSourceDocument).getStringValue();
  }

  public static void applyHandlingToNodes(
      Document pSourceDocument, XPath xpath, NodeHandler handler) {
    List<Node> nodes = xpath.selectNodes(pSourceDocument);
    for (Node node : nodes) {
      handler.handle(node);
    }
  }

  private static void setNameSpaces(String[] namespaces, XPath xpathDestination) {
    if (namespaces == null || namespaces.length == 0) {
      return;
    }
    Map<String, String> nameSpaceMap = new HashMap();
    for (String tuple : namespaces) {
      String[] tupleArr = tuple.split("=");
      nameSpaceMap.put(tupleArr[0], tupleArr[1]);
    }
    xpathDestination.setNamespaceContext(new SimpleNamespaceContext(nameSpaceMap));
  }

  /**
   * @param documentPath prefix "classpath:" may be used and expanded to source folder
   * @return
   * @throws IOException
   * @throws DocumentException
   */
  public static Document openDocument(String documentPath) throws IOException, DocumentException {
    return DocumentHelper.parseText(
        new String(Files.readAllBytes(Paths.get(expandIfPrefixedClassPath(documentPath)))));
  }
}
