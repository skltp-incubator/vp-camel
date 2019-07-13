package json;

import static org.junit.Assert.*;
import static wsdl.PathHelper.expandIfPrefixedClassPath;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import testutil.VPStringUtil;
import wsdl.WsdlConfig;

public class VPJSonHelperTest {

  private VPJSonHelper jsonHelper;

  @Before
  public void init() {
    jsonHelper = new VPJSonHelper();
  }

  @Test
  public void jsonFileToObjAndBacAgain() throws IOException {
    File testFile = new File(expandIfPrefixedClassPath(UtilConfigConstants.TEST_WSDL_CONFIG_FILE));
    assertTrue(testFile.exists());
    assertTrue(testFile != null);

    List<WsdlConfig> wsdlConfigList = jsonHelper.openWsdlConfigs(testFile);

    wsdlConfigList.forEach(
        config -> {
          assertTrue(VPStringUtil.hasANonEmptyValue(config.getTjanstekontrakt()));
          assertTrue(VPStringUtil.hasANonEmptyValue(config.getWsdlurl()));
          assertTrue(VPStringUtil.hasANonEmptyValue(config.getWsdlfilepath()));
        });

    File tmp = new File("tempJson.json");
    try (PrintWriter out = new PrintWriter(tmp)) {
      out.println(jsonHelper.object2Json(wsdlConfigList));
    }

    List<WsdlConfig> wsdlConfigList2 = jsonHelper.openWsdlConfigs(testFile);


    assertTrue(wsdlConfigList2.containsAll(wsdlConfigList));
    assertTrue(tmp.delete());
  }
}
