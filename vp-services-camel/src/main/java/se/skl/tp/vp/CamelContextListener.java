package se.skl.tp.vp;

import org.apache.camel.CamelContext;
import org.apache.camel.StartupListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import se.skl.tp.hsa.cache.HsaCache;

import static se.skl.tp.vp.constants.ApplicationProperties.HSA_FILES;

public class CamelContextListener implements StartupListener {
    private final Environment env;
    private final HsaCache hsaCache;

    public CamelContextListener(@Autowired Environment env, @Autowired HsaCache hsaCache) {
        this.env = env;
        this.hsaCache = hsaCache;
    }

    @Override
    public void onCamelContextStarted(CamelContext camelContext, boolean b) throws Exception {
        initHSACache();
        initTakCache();
    }

    private void initHSACache() {
        String[] hsaFiles = env.getProperty(HSA_FILES).split(",");
        hsaCache.init(hsaFiles);
    }
    private void initTakCache() {

    }
}