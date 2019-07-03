package se.skl.tp.vp.wsdl;

public class WsdlPathHelper {
  private static String PATH_PREFIX = "classpath:";

  /**
   * Expands a path that is relative to the resources dir example:
   * classpath:testfiles/wsdl/wsdlconfig.json expands to a path similar to->
   * C:/Your/file/path/to/target/testfiles/wsdl/wsdlconfig.json
   *
   * but leaves paths without prefix "classpath:" as is
   * @param pfilePath candidate
   * @return
   */
  public static String expandIfPrefixedClassPath(String pfilePath) {
    String result = pfilePath;
    if (result.startsWith(PATH_PREFIX)) {
      result =
          Thread.currentThread()
              .getContextClassLoader()
              .getResource(pfilePath.substring(PATH_PREFIX.length()))
              .getFile()
              .substring(1);
    }
    return result;
  }
}
