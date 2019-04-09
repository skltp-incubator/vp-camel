package se.skl.tp.vp.constants;

import static se.skl.tp.vp.httpheader.HttpHeaderFilterRegexp.getDefaultKeepRegExp;
import static se.skl.tp.vp.httpheader.HttpHeaderFilterRegexp.getDefaultRemoveRegExp;

import java.util.Map;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;

public class HttpHeaderFilterRegexpTest extends CamelTestSupport {
  private HeadersTestData testData;

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Before
  public void init() {
    testData = new HeadersTestData();
  }

  @Test
  public void testDefaultRemoveRegExp() {
    String regExp = getDefaultRemoveRegExp();
    for (String header : testData.getRemovers()) {
      assertTrue(header.matches(regExp));
    }
  }

  @Test
  public void testDefaultKeepRegExp() {
    String regExp = getDefaultKeepRegExp();
    for (String header : testData.getKeepers()) {
      assertTrue(header.matches(regExp));
    }
  }

  @Test
  public void testRemoveHeaders() {
    String anyBody = "<any/>";
    template.sendBodyAndHeaders(anyBody, testData.getAllTestHeaders());
    Map<String, Object> inMessageHeadersAtEndPoint =
        resultEndpoint.getExchanges().get(0).getIn().getHeaders();
    assertTrue(keepHeadersIsEquivalentToExpected(inMessageHeadersAtEndPoint));
  }

  private boolean keepHeadersIsEquivalentToExpected(Map<String, Object> candidate) {
    if (notSameSizeAsExpected(candidate)) {
      return false;
    }
    for (String header : candidate.keySet()) {
      if (!testData.getExpectedHeadersIgnoreCase().contains(header)) {
        return false;
      }
    }
    return true;
  }

  private boolean notSameSizeAsExpected(Map<String, Object> map1) {
    return map1.keySet().size() != testData.getExpectedHeadersIgnoreCase().size();
  }

  @Override
  protected RouteBuilder createRouteBuilder() {
    return new RouteBuilder() {
      public void configure() {
        from("direct:start")
            .removeHeaders(getDefaultRemoveRegExp(), getDefaultKeepRegExp())
            .to("mock:result");
      }
    };
  }
}
