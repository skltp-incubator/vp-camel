package se.skl.tp.vp.vagval;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.vp.Application;
import se.skltp.tak.vagval.wsdl.v2.ResetVagvalCacheResponse;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.TakCacheLog;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class ResetTakCacheProcessorTest extends CamelTestSupport {
    @Autowired
    private ResetTakCacheProcessor processor;

    @MockBean(name = "takCache")
    private TakCache takCacheMock;

    private List<String> testLog = new ArrayList<>();
    private String log1 =  "Test log1";
    private String log2 =  "Test log2";

    @Before
    public void beforeTest() {
        testLog.add(log1);
        testLog.add(log2);

        TakCacheLog takCacheLog = mock(TakCacheLog.class);
        Mockito.when(takCacheLog.getLog()).thenReturn(testLog);
        Mockito.when(takCacheMock.refresh()).thenReturn(takCacheLog);
    }

    @Test
    public void testResetIsOK() throws Exception {
        Exchange ex = createExchange();
        processor.process(ex);
        assertStringContains(ex.getOut().getBody(String.class), log1);
        assertStringContains(ex.getOut().getBody(String.class), log2);
    }

    private Exchange createExchange() {
        CamelContext ctx = new DefaultCamelContext();
        return new DefaultExchange(ctx);
    }

}
