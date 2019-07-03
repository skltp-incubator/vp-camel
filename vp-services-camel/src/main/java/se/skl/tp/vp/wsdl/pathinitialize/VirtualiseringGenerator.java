package se.skl.tp.vp.wsdl.pathinitialize;

import static se.skl.tp.vp.wsdl.pathinitialize.WsdlPathHelper.findFoldersInDirectory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class VirtualiseringGenerator {

  private VirtualiseringsGeneratorValidation validation = new VirtualiseringsGeneratorValidation();

  public void generate(String directoryPath) throws IOException {
    List<File> serviceInteractionDirectories =
        findFoldersInDirectory(directoryPath, ".*Interaction$");

    List<File> coreSchemaDirectory = findFoldersInDirectory(directoryPath, "core_components");

    validation.checkDirectoriesAndFiles(serviceInteractionDirectories);
  }
}
