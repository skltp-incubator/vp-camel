package extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class JarFilesUnZipper {

  JarFilesUnZipper(EntryFilter filter) {
    this.filter = filter;
  }

  private EntryFilter filter;

  void unzipXsdAndWsdlFilesTo(JarFile source, Path dest) throws IOException {
    makeDir(dest);
    Enumeration<JarEntry> entries = source.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if (filter == null || !filter.filter(entry)) {
        copyEntry(source, dest, entry);
      }
    }
  }

  private void makeDir(Path dir) {
    if (!dir.toFile().exists()&&!dir.toFile().mkdirs()) {
        throw new JarZipperException("Unable to make dir: " + dir);
    }
  }

  private void copyEntry(JarFile jarFile, Path dest, JarEntry entry) throws IOException {
    if (entry.isDirectory()) {
      makeDir(Paths.get(dest.toFile().getAbsolutePath(), entry.getName()));
    } else {
      saveStream(
          jarFile.getInputStream(entry),
          Paths.get(dest.toFile().getAbsolutePath(), entry.getName()).toFile());
    }
  }

  private void saveStream(InputStream initialStream, File target) throws IOException {
    try {
      java.nio.file.Files.copy(initialStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } finally {
      initialStream.close();
    }
  }

  public class JarZipperException extends RuntimeException {
    JarZipperException(String msg){
      super(msg);
    }
  }
}
