package se.skl.tp.vp.vagval;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {"se.skltp.takcache", "se.skl.tp.hsa.cache", "se.skl.tp.vp.vagval"})
public class VagvalTestConfiguration {

}
