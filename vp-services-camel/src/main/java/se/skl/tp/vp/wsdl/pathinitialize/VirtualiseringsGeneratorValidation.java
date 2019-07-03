package se.skl.tp.vp.wsdl.pathinitialize;

import static se.skl.tp.vp.wsdl.XmlHelper.openDocument;
import static se.skl.tp.vp.wsdl.pathinitialize.WsdlPathHelper.findFilesInDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.dom4j.Document;
import org.dom4j.DocumentException;

public class VirtualiseringsGeneratorValidation {
  private static int I_MULTIPLE_FILES_FOUND = 0;
  private static int I_INVALID_VERSION = 1;

  private StringBuilder errorMessage=new StringBuilder();

  // Matches strings ending with .xsd but not containing "_ext"
  public static String MATCH_XSD_FILES_NON_EXT = "((?!_ext).)*\\.xsd";
  public static String MATCH_END_IN_COLON_SINGEL_DIGIT = ".*:\\d$";

  private void addToErrorMessage(String... tokens){
    Arrays.asList(tokens).forEach(token->errorMessage.append(token));
  }

  private void insertToErrorMessage(String... tokens){
    Arrays.asList(tokens).
  }

  private void validateMatchingXsd(
      File serviceInteractionDirectoryCandidate,
      AtomicBoolean[] errorFoundDesination,
      StringBuilder errorMessageDestination)
      throws IOException, DocumentException {

    List<File> xsdFiles =
        findFilesInDirectory(
            serviceInteractionDirectoryCandidate.getPath(), MATCH_XSD_FILES_NON_EXT);

    if (xsdFiles.size() != 1) {
      errorFoundDesination[I_MULTIPLE_FILES_FOUND].set(true);
      errorMessageDestination
          .append("ERROR: Found more than one XSD file in ")
          .append(serviceInteractionDirectoryCandidate)
          .append(System.lineSeparator());
      xsdFiles.forEach(file -> errorMessageDestination.append(file).append(System.lineSeparator()));
    } else {

      Document xsd = openDocument(xsdFiles.get(0).getAbsolutePath());
      List<String> namespaces = xsd.getRootElement().declaredNamespaces();
      namespaces.forEach(
          namespace -> {
            if (!namespace.matches(MATCH_END_IN_COLON_SINGEL_DIGIT)) {
              errorFoundDesination[I_INVALID_VERSION].set(true);
              errorMessageDestination
                  .append("Incorrect version number for the following service contract!")
                  .append(System.lineSeparator())
                  .append("In ameSpace: ")
                  .append(namespace)
                  .append(System.lineSeparator());
            }
          });
    }
  }

  private void validateNumberOfwsdlFilesInDirectory(
      File serviceInteractionDirectoryCandidate,
      AtomicBoolean[] validationState,
      StringBuilder errorMessageDetination)
      throws IOException {

    if (findFilesInDirectory(serviceInteractionDirectoryCandidate.getPath(), ".*\\.wsdl").size()
        != 1) {
      validationState[I_MULTIPLE_FILES_FOUND].set(true);
      errorMessageDetination
          .append("ERROR: Found multiple WSDL files in ")
          .append(serviceInteractionDirectoryCandidate)
          .append(System.getProperty("line.separator"))
          .append("Please only supply one WSDL file for each contract!")
          .append("line.separator");
    }
  }

  public void checkDirectoriesAndFiles(List<File> serviceInteractionDirectories)
      throws IOException, DocumentException {
    errorMessage.setLength(0);

    AtomicBoolean[] validationState = {new AtomicBoolean(false), new AtomicBoolean(false)};
    for (File serviceInteractionDirectory : serviceInteractionDirectories) {
      validateNumberOfwsdlFilesInDirectory(serviceInteractionDirectory, validationState, errorMessage);
      validateMatchingXsd(serviceInteractionDirectory, validationState, errorMessage);
    }

    if (validationState[I_MULTIPLE_FILES_FOUND].get() || validationState[I_INVALID_VERSION].get()) {
      raiseException(errorMessage, validationState);
    }
  }

  private void raiseException(StringBuilder errorMessage, AtomicBoolean[] validationState) {

    if (validationState[I_MULTIPLE_FILES_FOUND].get()) {
      errorMessage
          .insert(0, System.lineSeparator())
          .insert(0, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
          .insert(0, "Found multiple XSD and/or WSDL files")
          .insert(0, System.lineSeparator())
          .insert(0, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    if (validationState[I_INVALID_VERSION].get()) {
      errorMessage
          .insert(0, System.lineSeparator())
          .insert(0, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
          .insert(0, "Found incorrect versions in namespaces")
          .insert(0, System.lineSeparator())
          .insert(0, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
    errorMessage
        .insert(0, System.lineSeparator())
        .insert(0, "VALIDATION ERROR ON WSDL DIRECTORY");
  }
}
