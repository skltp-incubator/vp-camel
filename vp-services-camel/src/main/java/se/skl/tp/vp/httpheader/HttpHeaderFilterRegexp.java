package se.skl.tp.vp.httpheader;

import static se.skl.tp.vp.constants.HttpHeaders.getAllHeaders;

import se.skl.tp.vp.VPRouter;

/**
 *
 */
public class HttpHeaderFilterRegexp {

  private static final String fDefaultKeepersRegExp = "(?i)"+String.join("|",getAllHeaders())+"|"+"(x-rivta.*)";

  private static final String fDefaultRemoversRegExp = ".*";

  /**
   * @see VPRouter#configure()
   * @see org.apache.camel.model.ProcessorDefinition#removeHeaders(String, String...)
   * @return a regular expression matching headers we normally dont want to keep
   */
  public static String getDefaultRemoveRegExp(){
    return fDefaultRemoversRegExp;
  }

  /**
   * @see VPRouter#configure()
   * @see org.apache.camel.model.ProcessorDefinition#removeHeaders(String, String...)
   * @return a regular expression matching headers we normally dont want to remove
   */
  public static String getDefaultKeepRegExp(){
    return fDefaultKeepersRegExp;
  }

}
