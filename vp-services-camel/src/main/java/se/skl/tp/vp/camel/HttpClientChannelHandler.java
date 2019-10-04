package se.skl.tp.vp.camel;

/**
 This is a override of the org.apache.camel.component.netty4.http.handlers.HttpClientChannelHandler version 2.24
 The intention is to implement ant test handling of HTTP respone 100-Continue, and later provide it for Camel.
 */

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.netty4.NettyConstants;
import org.apache.camel.component.netty4.http.NettyHttpProducer;

/**
 * Netty HTTP {@link org.apache.camel.component.netty4.handlers.ClientChannelHandler} that handles the response combine
 * back from the HTTP server, called by this client.
 */
public class HttpClientChannelHandler extends ClientChannelHandler {
  private final NettyHttpProducer producer;

  public HttpClientChannelHandler(NettyHttpProducer producer) {
    super(producer);
    this.producer = producer;
  }

  @Override
  protected Message getResponseMessage(Exchange exchange, ChannelHandlerContext ctx, Object message) throws Exception {
    FullHttpResponse response = (FullHttpResponse) message;

    if(response.status().code() == 100){
      exchange.setProperty("continue", true);
    } else if (!HttpUtil.isKeepAlive(response)) {
      // just want to make sure we close the channel if the keepAlive is not true
      exchange.setProperty(NettyConstants.NETTY_CLOSE_CHANNEL_WHEN_COMPLETE, true);
    }
    // handle cookies
    if (producer.getEndpoint().getCookieHandler() != null) {
      String actualUri = exchange.getIn().getHeader(Exchange.HTTP_URL, String.class);
      URI uri = new URI(actualUri);
      Map<String, List<String>> m = new HashMap<>();
      for (String name : response.headers().names()) {
        m.put(name, response.headers().getAll(name));
      }
      producer.getEndpoint().getCookieHandler().storeCookies(exchange, uri, m);
    }
    // use the binding
    return producer.getEndpoint().getNettyHttpBinding().toCamelMessage(response, exchange, producer.getConfiguration());
  }
}