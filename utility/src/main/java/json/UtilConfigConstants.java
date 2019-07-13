package json;

import static wsdl.PathHelper.PATH_PREFIX;

import testutil.VPStringUtil;

public class UtilConfigConstants {

  private UtilConfigConstants(){}

  public static final String TEST_WSDL_CONFIG_FILE =
      VPStringUtil.concat(PATH_PREFIX, "testWsdlConfigFile.json");
}
