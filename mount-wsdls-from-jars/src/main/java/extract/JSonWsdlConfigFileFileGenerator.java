package extract;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import json.VPJSonHelper;
import wsdl.WsdlConfig;

/**
 * Best case scenario Generates a file suitable for further tests of config + actual config file And
 * in the event that source contains problematic WsdlConfigHolder Eg. during extraction som Jar file
 * where found to not contain expected data.
 */
class JSonWsdlConfigFileFileGenerator {

  private VPJSonHelper vpjSonHelper = new VPJSonHelper();

  List<File> make(FileGenerationData pSource, String destinationDir)
      throws FileNotFoundException, JsonProcessingException {
    List<File> result = new ArrayList<>();
    File problematicJars = Paths.get(destinationDir, "problematicJars.txt").toFile();
    File jsonSettings = Paths.get(destinationDir, "WsdlConfigSettings.json").toFile();
    File testFile = Paths.get(destinationDir, "WsdlConfigSettingsTest.json").toFile();

    StringBuilder problematicJarsData = new StringBuilder();
    List<WsdlConfig> wsdlSettings = new ArrayList<>();
    List<String> testFileData = new ArrayList<>();
    pSource.getWsdlConfigurationSource().forEach(
        wsdlConfigHolder -> {
          if (wsdlConfigHolder.hasAnyProblem()) {
            problematicJarsData.append(wsdlConfigHolder.getProblem());
            problematicJarsData.append(System.lineSeparator());
          } else {
            WsdlConfig config = wsdlConfigHolder.getWsdlConfig();
            wsdlSettings.add(config);
            testFileData.add(config.getTjanstekontrakt());
          }
        });
    try (PrintWriter out = new PrintWriter(problematicJars)) {
      out.println(problematicJarsData.toString());
      result.add(problematicJars);
    }
    vpjSonHelper.objectToJsonFile(wsdlSettings, jsonSettings);
    result.add(jsonSettings);
    vpjSonHelper.objectToJsonFile(testFileData, testFile);
    result.add(testFile);

    return result;
  }
}
