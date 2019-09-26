package se.skl.tp.vp.camel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.component.netty4.ClientInitializerFactory;
import org.apache.camel.component.netty4.NettyProducer;
import org.apache.camel.component.netty4.http.NettyHttpConfiguration;
import org.apache.camel.component.netty4.http.NettyHttpProducer;
import org.apache.camel.component.netty4.http.handlers.HttpClientChannelHandler;
import org.apache.camel.util.ObjectHelper;
import org.springframework.stereotype.Component;

/*
This is a override of HttpClientInitializerFactory class from
camel-netty4-http component.To configure the netty4-http component
to use this class use the "clientInitializerFactory" configuration option.

The intention of this class is to implement handling the TLS SNI extension.
 */
@Log4j2
@Component
public class VPHttpClientPipelineFactory extends ClientInitializerFactory {

  protected NettyHttpConfiguration configuration;
  protected NettyHttpProducer nettyProducer;
  protected SSLContext sslContext;

  public VPHttpClientPipelineFactory() {
    // default constructor needed
  }

  public VPHttpClientPipelineFactory(NettyHttpProducer nettyProducer) {
    this.nettyProducer = nettyProducer;
    configuration = nettyProducer.getConfiguration();

    if (configuration.isSsl()) {
      try {
        this.sslContext = createSSLContext(nettyProducer);
        log.info("Created SslContext {}", sslContext);
      } catch (Exception e) {
        throw ObjectHelper.wrapRuntimeCamelException(e);
      }
    }
  }

  @Override
  public ClientInitializerFactory createPipelineFactory(NettyProducer nettyProducer) {
    return new VPHttpClientPipelineFactory((NettyHttpProducer) nettyProducer);
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    // create a new pipeline
    ChannelPipeline pipeline = ch.pipeline();

    if (configuration.isSsl()) {
      SslHandler sslHandler = configureClientSSLOnDemand();
      log.debug("Client SSL handler configured and added as an interceptor against the ChannelPipeline: {}", sslHandler);
      pipeline.addLast("ssl", sslHandler);
    }

    pipeline.addLast("http", new HttpClientCodec());
    pipeline.addLast("aggregator", new HttpObjectAggregator(configuration.getChunkedMaxContentLength()));

    if (configuration.getRequestTimeout() > 0) {
      if (log.isTraceEnabled()) {
        log.trace("Using request timeout {} millis", configuration.getRequestTimeout());
      }
      ChannelHandler timeout = new ReadTimeoutHandler(configuration.getRequestTimeout(), TimeUnit.MILLISECONDS);
      pipeline.addLast("timeout", timeout);
    }

    // handler to route Camel messages
    pipeline.addLast("handler", new HttpClientChannelHandler(nettyProducer));
  }

  private SSLContext createSSLContext(NettyProducer producer) throws Exception {

    if (configuration.getSslContextParameters() == null) {
      log.error("No getSslContextParameters configured for this ssl connection");
      return null;
    }

    return configuration.getSslContextParameters().createSSLContext(producer.getContext());
  }

  private SslHandler configureClientSSLOnDemand() throws Exception {

    if (sslContext != null) {
      URI uri = new URI(nettyProducer.getEndpoint().getEndpointUri());
//      SSLEngine sllEngine = sslContext.createSSLEngine();
      SSLEngine sllEngine = sslContext.createSSLEngine(uri.getHost(), uri.getPort());
      sllEngine.setUseClientMode(true);
      SSLParameters sslParameters = sllEngine.getSSLParameters();
      sslParameters.setServerNames(new ArrayList<SNIServerName>(1) {{
        add(new SNIHostName(uri.getHost()));
      }});
      sllEngine.setSSLParameters(sslParameters);
      SslHandler sslHandler = new SslHandler(sllEngine);
      //TODO must close on SSL exception
      // sslHandler.setCloseOnSSLException(true);
      return sslHandler;
    }

    return null;
  }


}
