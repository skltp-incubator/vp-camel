package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.MessageHistory;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import se.skl.tp.vp.constants.HttpHeaders;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MeasureWorkTimeProcessor implements Processor {
    public static final String HTTP_NODE_ID = "httpNodeId";
    public static final String HTTPS_NODE_ID = "httpsNodeId";

    @Override
    public void process(Exchange exchange) {
        List<MessageHistory> list = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);
        List<Long> time = list.stream().filter(line ->
                HTTP_NODE_ID.equals(line.getNode().getId()) && line.getElapsed() != 0 ||
                HTTPS_NODE_ID.equals(line.getNode().getId()) && line.getElapsed() != 0)
                .map(MessageHistory::getElapsed)
                .collect(Collectors.toList());


        Message message = exchange.getIn();
        message.setHeader(HttpHeaders.X_SKLTP_PRODUCER_RESPONSETIME, time.get(0));
    }
}
