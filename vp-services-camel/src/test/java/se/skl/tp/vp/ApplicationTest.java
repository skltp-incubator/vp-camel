package se.skl.tp.vp;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.EnableRouteCoverage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(CamelSpringBootRunner.class)

@EnableRouteCoverage
public class ApplicationTest {

    @Autowired
    private CamelContext camelContext;

    @Test
    public void contextLoads(){

    }

}