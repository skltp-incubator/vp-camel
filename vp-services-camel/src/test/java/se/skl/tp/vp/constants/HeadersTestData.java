package se.skl.tp.vp.constants;

import static se.skl.tp.vp.integrationtests.utils.CaseRandomize.randomCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HeadersTestData {

  private static final List<String> EXPECTED_HEADERS = new ArrayList(
  Arrays.asList("content-length"
      ,"x-rivta-original-serviceconsumer-hsaid"
      ,"x-skltp-correlation-id"
      ,"x-vp-instance-id"
      ,"connection"
      ,"X-Forwarded-For"
      ,"host"
      ,"x-vp-sender-id"
      ,"breadcrumbId"
      ,"User-Agent"
      ,"Content-Type"
      ,"SoapAction"
      ));

  private  Set<String> expectedHeadersIgnoreCase;

  private Set<String> removers;
  private Set<String> keepers;
  private Map<String, Object> allTestHeaders;

  /**
   * Place holder for test data "headers" for testing removal/filtering of headers
   *
   */
  public HeadersTestData() {

    expectedHeadersIgnoreCase = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    allTestHeaders = new HashMap<>();

    initKeepers();
    initRemovers();
    initExpectedAndTestHeaders();
  }

  /**
   * @see #initExpectedAndTestHeaders()
   * @return headers including both keepers and removers
   */
  public Map<String,Object> getAllTestHeaders(){
    return allTestHeaders;
  }

  private void initExpectedAndTestHeaders() {

    expectedHeadersIgnoreCase.addAll(EXPECTED_HEADERS);

    int i = 0;
    for (String header : keepers) {
      allTestHeaders.put(header, "Value" + i);
      i++;
    }
    for (String header : removers) {
      allTestHeaders.put(header, "Value" + i);
      i++;
    }

  }

  /**
   * @see #getKeepers()
   * @return a set equivalent to keepers but where contains ignores case
   */
  public Set<String> getExpectedHeadersIgnoreCase() {
    return expectedHeadersIgnoreCase;
  }
  /**
   * Note that current implementation of the Remove RegExp* is greedy and remove more or less all.
   * Eg. current implementation rely on the keeper section to do the job.
   *
   * @see #initRemovers()
   * @return Returns a set of "headers" that the remove RegExp* should match
   */
  public Set<String> getRemovers() {
    return removers;
  }

  /**
   * @see #initKeepers()
   * @return Returns a set of "headers" that the keep RegExp* should match
   */
  public Set<String> getKeepers() {
    return keepers;
  }

  /**
   * Add headers/header patterns that should always bee removed before message forwarded to other
   * system
   */
  private void initRemovers() {
    removers = new HashSet<>(
        Arrays.asList(
            randomCase("rivversion"),
            randomCase("wsdl_namespace"),
            randomCase("x-vp-auth-cert"),
            randomCase("servicecontract_namespace"),
            randomCase("http.disable.status.code.exception.check"),
            randomCase("PEER_CERTIFICATES")
        )
    );


  }

  /** Add any test headers that system always should keep (forward to other systems) */
  private void initKeepers() {
    keepers= new HashSet<>();
    for (int i = 0; i < 10; i++) {
      keepers.add(randomCase("x-rivta" + "header" + i));
    }
  }


}
