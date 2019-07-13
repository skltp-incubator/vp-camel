package testutil;

import static org.junit.Assert.*;
import static testutil.VPStringUtil.concat;
import static testutil.VPStringUtil.hasANonEmptyValue;
import static testutil.VPStringUtil.inputStream2UTF8Str;
import static testutil.VPStringUtil.valueIsEmpty;
import static wsdl.PathHelper.PATH_PREFIX;
import static wsdl.PathHelper.expandIfPrefixedClassPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class VPStringUtilTest {

  @Test
  public void testConcat() {
    assertTrue(concat("h", "e", "j", " världen är allt okej").matches("hej världen är allt okej"));
  }

  @Test
  public void testHasANonEmptyValue() {
    assertTrue(
        hasANonEmptyValue("hej världen är allt okej")
            && !hasANonEmptyValue(null)
            && !hasANonEmptyValue("")
            && !hasANonEmptyValue(" ")
            && hasANonEmptyValue(" A ")
            && hasANonEmptyValue("B ")
            && hasANonEmptyValue(" C"));
  }

  @Test
  public void testvalueIsEmpty() {
    assertTrue(
        !valueIsEmpty("hej världen är allt okej")
            && valueIsEmpty(null)
            && valueIsEmpty("")
            && valueIsEmpty(" ")
            && !valueIsEmpty(" A ")
            && !valueIsEmpty("B ")
            && !valueIsEmpty(" C")
            && valueIsEmpty(concat("\t",System.lineSeparator()," "))
            && !valueIsEmpty(concat("\t",System.lineSeparator(),"oj"," ")));
  }

  @Test
  public void testInputStream2UTF8Str() throws IOException {

     String s  = inputStream2UTF8Str(new FileInputStream(new File(expandIfPrefixedClassPath(concat(PATH_PREFIX,"UTF8.txt")))));
    assertTrue(s.equals("Åäö hej"));
  }


}
