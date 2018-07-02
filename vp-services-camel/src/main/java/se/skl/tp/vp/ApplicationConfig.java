package se.skl.tp.vp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.hsa.cache.HsaCacheImpl;

import static se.skl.tp.vp.constants.ApplicationProperties.HSA_FILES;

@Configuration
@ComponentScan(basePackages = {"se.skltp.takcache"})
public class ApplicationConfig {
    private final Environment env;

    @Autowired
    public ApplicationConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public HsaCache hsaCache(){
        String [] hsaFiles = env.getProperty(HSA_FILES).split(",");
        HsaCache hsaCache = new HsaCacheImpl();
        hsaCache.init(hsaFiles);
        return hsaCache;
    }
}
