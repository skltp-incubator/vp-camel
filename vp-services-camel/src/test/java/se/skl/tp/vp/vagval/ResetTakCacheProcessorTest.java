package se.skl.tp.vp.vagval;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.vp.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class ResetTakCacheProcessorTest extends CamelTestSupport {
    @Autowired
    ResetTakCacheProcessor processor;

    @Test
    public void testResetIsOK() throws Exception {
        Exchange ex = createExchange();
        processor.process(ex);
//        assertStringContains(ex.getOut().getBody(String.class), "true");
    }

    private Exchange createExchange() {
        CamelContext ctx = new DefaultCamelContext();
        return new DefaultExchange(ctx);
    }

}
