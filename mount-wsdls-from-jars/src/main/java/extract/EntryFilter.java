package extract;

import java.util.jar.JarEntry;

public interface EntryFilter {
  boolean filter(JarEntry entry);
}
