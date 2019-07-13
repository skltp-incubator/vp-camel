package extract;

import static org.junit.Assert.*;
import static testutil.VPStringUtil.valueIsEmpty;
import static wsdl.PathHelper.deleteDirectory;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;
import wsdl.PathHelper;

public class ExtractWsdlDataFromOldVPJarsTest {



  @Test
  public void extract() {
    String sourceFolder = PathHelper.expandIfPrefixedClassPath("classpath:testfiles/jarTestFiles"); //PathHelper.expandIfPrefixedClassPath(VPStringUtil.concat(PATH_PREFIX,"testfiles"));
    String destinationFolder = Paths.get(sourceFolder,"VP").toString();

    String exceptionMessage = null;
    ExtractWsdlDataFromOldVPJars dataExtractor = new ExtractWsdlDataFromOldVPJars();
    assertTrue(dataExtractor != null);

    try {

      List<File> result =
          dataExtractor.mountExtractedWsdlAndCreateSettingFiles(sourceFolder, destinationFolder);

      File destination = new File(destinationFolder);

      deleteDirectory(Paths.get(destinationFolder));

      assertTrue((result.size()>1));

      assertFalse(destination.exists());
    } catch (Exception e) {
      exceptionMessage = e.getMessage();
    }
    assertTrue(exceptionMessage, valueIsEmpty(exceptionMessage));
  }
}
