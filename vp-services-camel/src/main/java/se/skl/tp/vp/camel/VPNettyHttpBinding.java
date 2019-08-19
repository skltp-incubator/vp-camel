package se.skl.tp.vp.camel;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import java.net.URI;
import org.apache.camel.Message;
import org.apache.camel.component.netty4.http.DefaultNettyHttpBinding;
import org.apache.camel.component.netty4.http.NettyHttpConfiguration;
import org.springframework.stereotype.Component;

@Component
public class VPNettyHttpBinding extends DefaultNettyHttpBinding {

  @Override
  public HttpRequest toNettyRequest(Message message, String uri, NettyHttpConfiguration configuration) throws Exception {
    // DefaultNettyHttpBinding will in some situations set port to -1 in the
    // in the HTTP "host" header. This will cause Apache proxy server to return a
    // HTTP 400 error. Therefor override DefaultNettyHttpBinding and change the
    // default "host header".
    // See ch. “14.23 Host”  in https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
    HttpRequest request = super.toNettyRequest(message, uri, configuration);
    URI u = new URI(uri);
    int port = u.getPort();
    String hostHeader = u.getHost() + (port == 80 || port ==-1 ? "" : ":" + u.getPort());
    request.headers().set(HttpHeaderNames.HOST.toString(), hostHeader);
    return request;
  }

}
