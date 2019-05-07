package se.skl.tp.vp.constants;
import static junit.framework.TestCase.assertTrue;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.config.HttpHeaderFilterRegexp;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class HttpHeaderFilterRegexpTest {

  @Autowired
  private HttpHeaderFilterRegexp reg;

  private HeadersTestData testData;

  @Before
  public void init() {
    testData = new HeadersTestData();
 }
  @Test
  public void testDefaultRemoveRegExp() {
    String regExp = reg.getRemoveRegExp();
    for (String header : testData.getRemovers()) {
      assertTrue(header.matches(regExp));
    }
  }

  @Test
  public void testDefaultKeepRegExp() {
    String regExp = reg.getKeepRegExp();
    for (String header : testData.getKeepers()) {
      assertTrue(header.matches(regExp));
    }
  }


}
