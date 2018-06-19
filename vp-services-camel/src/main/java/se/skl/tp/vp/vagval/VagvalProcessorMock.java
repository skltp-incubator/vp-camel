package se.skl.tp.vp.vagval;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum;

@Service
public class VagvalProcessorMock implements VagvalProcessor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String receiverid = (String)exchange.getProperty(VPExchangeProperties.RECEIVER_ID);
        String servicecontractNamespace = (String)exchange.getProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE);
        if("urn:riv:clinicalprocess:healthcond:description:GetCareDocumentationResponder:2".equalsIgnoreCase(servicecontractNamespace)) {
            switch (receiverid) {
                case "1" : exchange.setProperty(VPExchangeProperties.VAGVAL, "http://localhost:8123/"); break;
                case "2" : exchange.setProperty(VPExchangeProperties.VAGVAL, "http://localhost:8234/"); break;
                default: exchange.setProperty("errorCode", VpSemanticErrorCodeEnum.VP004);
            }
        }
        else if ("urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1".equalsIgnoreCase(servicecontractNamespace)) {
            switch (receiverid) {
                case "1" : exchange.setProperty(VPExchangeProperties.VAGVAL, "http://localhost:8345/"); break;
                default: exchange.setProperty("errorCode", VpSemanticErrorCodeEnum.VP004);
            }
        }
    }
}
