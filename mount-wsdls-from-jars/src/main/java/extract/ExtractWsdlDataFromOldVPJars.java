package extract;

import static wsdl.PathHelper.findFilesInDirectory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

public class ExtractWsdlDataFromOldVPJars {

  private JarFilesUnZipper unzipper = new JarFilesUnZipper(new KeepWsdlAndXsdFilter());

  private WsdlUrlExtractor wsdlurlExtractor = new WsdlUrlExtractor();

  private UnpackedWdslFinder findUnpackedWsdl = new UnpackedWdslFinder();

  private JSonWsdlConfigFileFileGenerator fileGenerator = new JSonWsdlConfigFileFileGenerator();

  private FileGenerationData fileGenData = new FileGenerationData();

  public List<File> mountExtractedWsdlAndCreateSettingFiles(String sourceDir, String mountIn)
      throws IOException {

    List<File> jarFiles = findFilesInDirectory(sourceDir, ".*\\.jar");
    jarFiles.forEach(
        file -> {
          try (JarFile jarFile = new JarFile(file)) {

            if (wsdlurlExtractor.excecutedOk(jarFile)) {

              fileGenData.setWsdlurl(file, wsdlurlExtractor.wsdlUrl());
              unzipper.unzipXsdAndWsdlFilesTo(jarFile, wsdlurlExtractor.subDir(mountIn));
              if (findUnpackedWsdl.extractTjansteKontraktAndPath(
                  wsdlurlExtractor.subDir(mountIn),mountIn)) {
                fileGenData.setTjanstekontrakt(file, findUnpackedWsdl.serviceContract());
                fileGenData.setWsdlfilepath(file, findUnpackedWsdl.wsdlPath());
              } else {
                fileGenData.AddProblemWithExtractTjansteKontraktAndPath(
                    file, findUnpackedWsdl.lastExcecutionResult());
              }
            } else {
              fileGenData.addProblemWithWsdlurl(wsdlurlExtractor.excecutionResult(), file);
            }
          } catch (Exception e) {
            fileGenData.addProblemUnexpectedException(file, e);
          }
        });

    return fileGenerator.make(fileGenData, mountIn);
  }
}
