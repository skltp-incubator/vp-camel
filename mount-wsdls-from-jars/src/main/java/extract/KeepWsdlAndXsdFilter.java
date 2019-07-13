package extract;

import java.util.jar.JarEntry;

public class KeepWsdlAndXsdFilter implements EntryFilter {



  @Override
  public boolean filter(JarEntry entry) {
    return entry.isDirectory()
        ? !entry.getName().startsWith("schemas")
        : isNeitherSchemaOrWsdl(entry.getName());
  }

  private boolean isNeitherSchemaOrWsdl(String candidate) {
    return  !candidate.matches(".*\\.xsd$|.*\\.wsdl$");
  }
}
