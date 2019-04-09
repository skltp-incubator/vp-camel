package se.skl.tp.vp.constants;

import static se.skl.tp.vp.constants.HttpHeaders.getAllHeaders;
import static se.skl.tp.vp.integrationtests.utils.CaseRandomize.randomCase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HeadersTestData {
  private Set<String> expectedHeadersIgnoreCase;
  private Set<String> removers;
  private Set<String> keepers;
  private Map<String, Object> allTestHeaders;

  /**
   * Place holder for test data "headers" for testing removal/filtering of headers
   *
   */
  public HeadersTestData() {

    expectedHeadersIgnoreCase = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    removers = new HashSet<>();
    keepers = new HashSet<>();
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
    int i = 0;
    for (String header : keepers) {
      expectedHeadersIgnoreCase.add(header);
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
    removers = new HashSet<>();

    removers.add(randomCase("Camel-bla_bla"));

    removers.add(randomCase("Camel"));

    removers.add(randomCase("breadcrumbId"));
  }


  /** Add any test headers that system always should keep (forward to other systems) */
  private void initKeepers() {

    for (int i = 0; i < 10; i++) {
      keepers.add(randomCase("x-rivta" + "header" + i));
    }
    Set<String> knownHeaders = getAllHeaders();
    for (String header : knownHeaders) {
      keepers.add(randomCase(header));
    }

  }
}
