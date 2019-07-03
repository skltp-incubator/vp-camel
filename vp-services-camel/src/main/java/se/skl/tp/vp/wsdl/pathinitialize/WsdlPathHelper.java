package se.skl.tp.vp.wsdl.pathinitialize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WsdlPathHelper {
  public static List<File> findFoldersInDirectory(String directoryPath, String folderNameRegEx)
      throws IOException {
    List<File> res = new ArrayList<>();
    Files.walk(Paths.get(directoryPath))
        .filter(p -> Files.isDirectory(p) && p.toFile().getName().matches(folderNameRegEx))
        .forEach(p -> res.add(p.toFile()));
    return res;
  }

  public static List<File> findFilesInDirectory(String directoryPath, String fileNameRegEx)
      throws IOException {
    List<File> res = new ArrayList<>();
    Files.walk(Paths.get(directoryPath))
        .filter(p -> !Files.isDirectory(p) && p.toFile().getName().matches(fileNameRegEx))
        .forEach(p -> res.add(p.toFile()));
    return res;
  }


}
