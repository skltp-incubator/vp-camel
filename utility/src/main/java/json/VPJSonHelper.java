package json;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import wsdl.WsdlConfig;

public class VPJSonHelper {

  private ObjectMapper objJsonMapper = new ObjectMapper();

  public String object2Json(Object o) throws JsonProcessingException {
    return objJsonMapper.writeValueAsString(o);
  }

  public List<WsdlConfig> openWsdlConfigs(File f) throws IOException {
    return objJsonMapper.readValue(f, new TypeReference<List<WsdlConfig>>() {});
  }

  public void objectToJsonFile(Object o,File f)
      throws FileNotFoundException, JsonProcessingException {
    try (
        PrintWriter out = new PrintWriter(f)) {
      out.println(object2Json(o));
    }
  }

}
