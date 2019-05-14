package se.skl.tp.vp.service;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Arrays;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.hsa.cache.HsaCacheInitializationException;
import se.skl.tp.vp.constants.PropertyConstants;

@Log4j2
@Service
public class HsaCacheServiceImpl implements HsaCacheService {
  private final HsaCache hsaCache;
  private String[] hsaFiles;

  @Autowired
  public HsaCacheServiceImpl(@Value("${" + PropertyConstants.HSA_FILES + "}")String hsaFiles
                , HsaCache hsaCache) {
    this.hsaFiles = toFilesArray(hsaFiles);

    this.hsaCache = hsaCache;
  }

  private String[] toFilesArray(String hsaFiles) {
    String[] files = hsaFiles.split(",");
    for(int i=0; i<files.length;i++){
      if(files[i].startsWith("classpath:")) {
        files[i] = resourceToFullPath(files[i]);
      }
    }
    return files;
  }

  private String resourceToFullPath(String file) {
    try {
      URL url = ResourceUtils.getURL(file);
      return url.getFile();
    } catch (FileNotFoundException e) {
      return file;
    }
  }

  @Override
  public String resetCache() {
    String result = String.format("Start a reset of HSA cache using files: %s%n", Arrays.toString(hsaFiles));
    log.info(result);
    String logData;
    try {
      int oldCacheSize = hsaCache.getHSACacheSize();
      HsaCache cache = hsaCache.init(hsaFiles);
      int cacheSize = cache.getHSACacheSize();
      if (cacheSize > 1) {
        logData = String.format("Successfully reset HSA cache. %nHSA cache size was: %d %nHSA cache now is: %d.", oldCacheSize, cacheSize);
        log.info(logData);
        result+= logData;
      } else {
        logData = String.format("Warning: HSA cache reset to %d. Was %d entries!", cacheSize, oldCacheSize);
        log.warn(logData);
        result+=logData;
      }
    } catch (HsaCacheInitializationException e) {
      logData = "Reset HSA cache failed.";
      log.error(logData, e);
      result += logData + e.toString();
    }
    return result;
  }

}
